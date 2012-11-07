package net.minecraft.server;

import cpw.mods.fml.common.network.FMLNetworkHandler;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import javax.crypto.SecretKey;

public class NetLoginHandler extends NetHandler
{
    private byte[] field_72536_d;

    /** The Minecraft logger. */
    public static Logger logger = Logger.getLogger("Minecraft");

    /** The Random object used to generate serverId hex strings. */
    private static Random random = new Random();
    public NetworkManager networkManager;

    /**
     * Returns if the login handler is finished and can be removed. It is set to true on either error or successful
     * login.
     */
    public boolean c = false;

    /** Reference to the MinecraftServer object. */
    private MinecraftServer server;

    /** While waiting to login, if this field ++'s to 600 it will kick you. */
    private int g = 0;
    public String h = null;
    private volatile boolean field_72544_i = false;

    /** server ID that is randomly generated by this login handler. */
    private String loginKey = "";
    private SecretKey field_72542_k = null;

    public NetLoginHandler(MinecraftServer var1, Socket var2, String var3) throws IOException
    {
        this.server = var1;
        this.networkManager = new NetworkManager(var2, var3, this, var1.F().getPrivate());
        this.networkManager.field_74468_e = 0;
    }

    /**
     * Logs the user in if a login packet is found, otherwise keeps processing network packets unless the timeout has
     * occurred.
     */
    public void c()
    {
        if (this.field_72544_i)
        {
            this.d();
        }

        if (this.g++ == 6000)
        {
            this.disconnect("Took too long to log in");
        }
        else
        {
            this.networkManager.b();
        }
    }

    /**
     * Disconnects the user with the given reason.
     */
    public void disconnect(String var1)
    {
        try
        {
            logger.info("Disconnecting " + this.getName() + ": " + var1);
            this.networkManager.queue(new Packet255KickDisconnect(var1));
            this.networkManager.d();
            this.c = true;
        }
        catch (Exception var3)
        {
            var3.printStackTrace();
        }
    }

    public void a(Packet2Handshake var1)
    {
        this.h = var1.f();

        if (!this.h.equals(StripColor.a(this.h)))
        {
            this.disconnect("Invalid username!");
        }
        else
        {
            PublicKey var2 = this.server.F().getPublic();

            if (var1.d() != 47)
            {
                if (var1.d() > 47)
                {
                    this.disconnect("Outdated server!");
                }
                else
                {
                    this.disconnect("Outdated client!");
                }
            }
            else
            {
                this.loginKey = this.server.getOnlineMode() ? Long.toString(random.nextLong(), 16) : "-";
                this.field_72536_d = new byte[4];
                random.nextBytes(this.field_72536_d);
                this.networkManager.queue(new Packet253KeyRequest(this.loginKey, var2, this.field_72536_d));
            }
        }
    }

    public void a(Packet252KeyResponse var1)
    {
        PrivateKey var2 = this.server.F().getPrivate();
        this.field_72542_k = var1.func_73303_a(var2);

        if (!Arrays.equals(this.field_72536_d, var1.func_73302_b(var2)))
        {
            this.disconnect("Invalid client reply");
        }

        this.networkManager.queue(new Packet252KeyResponse());
    }

    public void a(Packet205ClientCommand var1)
    {
        if (var1.a == 0)
        {
            if (this.server.getOnlineMode())
            {
                (new ThreadLoginVerifier(this)).start();
            }
            else
            {
                this.field_72544_i = true;
            }
        }
    }

    public void a(Packet1Login var1)
    {
        FMLNetworkHandler.handleLoginPacketOnServer(this, var1);
    }

    /**
     * on success the specified username is connected to the minecraftInstance, otherwise they are packet255'd
     */
    public void d()
    {
        FMLNetworkHandler.onConnectionReceivedFromClient(this, this.server, this.networkManager.getSocketAddress(), this.h);
    }

    public void completeConnection(String var1)
    {
        if (var1 != null)
        {
            this.disconnect(var1);
        }
        else
        {
            EntityPlayer var2 = this.server.getServerConfigurationManager().processLogin(this.h);

            if (var2 != null)
            {
                this.server.getServerConfigurationManager().a(this.networkManager, var2);
            }
        }

        this.c = true;
    }

    public void a(String var1, Object[] var2)
    {
        logger.info(this.getName() + " lost connection");
        this.c = true;
    }

    /**
     * Handle a server ping packet.
     */
    public void a(Packet254GetInfo var1)
    {
        try
        {
            ServerConfigurationManagerAbstract var2 = this.server.getServerConfigurationManager();
            String var3 = null;

            if (var1.field_82559_a == 1)
            {
                List var4 = Arrays.asList(new Serializable[] {Integer.valueOf(1), Integer.valueOf(47), this.server.getVersion(), this.server.getMotd(), Integer.valueOf(var2.getPlayerCount()), Integer.valueOf(var2.getMaxPlayers())});
                Object var5;

                for (Iterator var6 = var4.iterator(); var6.hasNext(); var3 = var3 + var5.toString().replaceAll("\u0000", ""))
                {
                    var5 = var6.next();

                    if (var3 == null)
                    {
                        var3 = "\u00a7";
                    }
                    else
                    {
                        var3 = var3 + "\u0000";
                    }
                }
            }
            else
            {
                var3 = this.server.getMotd() + "\u00a7" + var2.getPlayerCount() + "\u00a7" + var2.getMaxPlayers();
            }

            InetAddress var8 = null;

            if (this.networkManager.getSocket() != null)
            {
                var8 = this.networkManager.getSocket().getInetAddress();
            }

            this.networkManager.queue(new Packet255KickDisconnect(var3));
            this.networkManager.d();

            if (var8 != null && this.server.ae() instanceof DedicatedServerConnection)
            {
                ((DedicatedServerConnection)this.server.ae()).func_71761_a(var8);
            }

            this.c = true;
        }
        catch (Exception var7)
        {
            var7.printStackTrace();
        }
    }

    /**
     * Default handler called for packets that don't have their own handlers in NetServerHandler; kicks player from the
     * server.
     */
    public void onUnhandledPacket(Packet var1)
    {
        this.disconnect("Protocol error");
    }

    public String getName()
    {
        return this.h != null ? this.h + " [" + this.networkManager.getSocketAddress().toString() + "]" : this.networkManager.getSocketAddress().toString();
    }

    /**
     * determine if it is a server handler
     */
    public boolean a()
    {
        return true;
    }

    /**
     * Returns the server Id randomly generated by this login handler.
     */
    static String a(NetLoginHandler var0)
    {
        return var0.loginKey;
    }

    /**
     * Returns the reference to Minecraft Server.
     */
    static MinecraftServer b(NetLoginHandler var0)
    {
        return var0.server;
    }

    static SecretKey func_72525_c(NetLoginHandler var0)
    {
        return var0.field_72542_k;
    }

    /**
     * Returns the connecting client username.
     */
    static String d(NetLoginHandler var0)
    {
        return var0.h;
    }

    public static boolean func_72531_a(NetLoginHandler var0, boolean var1)
    {
        return var0.field_72544_i = var1;
    }

    public void a(Packet250CustomPayload var1)
    {
        FMLNetworkHandler.handlePacket250Packet(var1, this.networkManager, this);
    }

    public void handleVanilla250Packet(Packet250CustomPayload var1) {}

    public EntityHuman getPlayer()
    {
        return null;
    }
}
