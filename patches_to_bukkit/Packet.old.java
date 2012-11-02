package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Packet
{
  public static IntHashMap j = new IntHashMap();
  private static Map a = new HashMap();
  private static Set b = new HashSet();
  private static Set c = new HashSet();
  public final long timestamp = System.currentTimeMillis();
  public static long l;
  public static long m;
  public static long n;
  public static long o;
  public boolean lowPriority = false;

  static void a(int i, boolean flag, boolean flag1, Class oclass)
  {
    if (j.b(i))
      throw new IllegalArgumentException(new StringBuilder().append("Duplicate packet id:").append(i).toString());
    if (a.containsKey(oclass)) {
      throw new IllegalArgumentException(new StringBuilder().append("Duplicate packet class:").append(oclass).toString());
    }
    j.a(i, oclass);
    a.put(oclass, Integer.valueOf(i));
    if (flag) {
      b.add(Integer.valueOf(i));
    }

    if (flag1)
      c.add(Integer.valueOf(i));
  }

  public static Packet a(int i)
  {
    try
    {
      Class oclass = (Class)j.get(i);

      return oclass == null ? null : (Packet)oclass.newInstance();
    } catch (Exception exception) {
      exception.printStackTrace();
      System.out.println(new StringBuilder().append("Skipping packet with id ").append(i).toString());
    }return null;
  }

  public final int b()
  {
    return ((Integer)a.get(getClass())).intValue();
  }

  public static Packet a(DataInputStream datainputstream, boolean flag) throws IOException
  {
    boolean flag1 = false;
    Packet packet = null;
    int i;
    try {
      i = datainputstream.read();
      if (i == -1) {
        return null;
      }

      if (((flag) && (!c.contains(Integer.valueOf(i)))) || ((!flag) && (!b.contains(Integer.valueOf(i))))) {
        throw new IOException(new StringBuilder().append("Bad packet id ").append(i).toString());
      }

      packet = a(i);
      if (packet == null) {
        throw new IOException(new StringBuilder().append("Bad packet id ").append(i).toString());
      }

      packet.a(datainputstream);
      l += 1L;
      m += packet.a();
    } catch (EOFException eofexception) {
      System.out.println("Reached end of stream");
      return null;
    }
    catch (SocketTimeoutException exception)
    {
      System.out.println("Read timed out");
      return null;
    } catch (SocketException exception) {
      System.out.println("Connection reset");
      return null;
    }

    PacketCounter.a(i, packet.a());
    l += 1L;
    m += packet.a();
    return packet;
  }

  public static void a(Packet packet, DataOutputStream dataoutputstream) throws IOException
  {
    dataoutputstream.write(packet.b());
    packet.a(dataoutputstream);
    n += 1L;
    o += packet.a();
  }

  public static void a(String s, DataOutputStream dataoutputstream) throws IOException
  {
    if (s.length() > 32767) {
      throw new IOException("String too big");
    }
    dataoutputstream.writeShort(s.length());
    dataoutputstream.writeChars(s);
  }

  public static String a(DataInputStream datainputstream, int i)
    throws IOException
  {
    short short1 = datainputstream.readShort();

    if (short1 > i)
      throw new IOException(new StringBuilder().append("Received string length longer than maximum allowed (").append(short1).append(" > ").append(i).append(")").toString());
    if (short1 < 0) {
      throw new IOException("Received string length is less than zero! Weird string!");
    }
    StringBuilder stringbuilder = new StringBuilder();

    for (int j = 0; j < short1; j++) {
      stringbuilder.append(datainputstream.readChar());
    }

    return stringbuilder.toString();
  }

  public abstract void a(DataInputStream paramDataInputStream) throws IOException;

  public abstract void a(DataOutputStream paramDataOutputStream) throws IOException;

  public abstract void handle(NetHandler paramNetHandler);

  public abstract int a();

  protected ItemStack b(DataInputStream datainputstream) throws IOException
  {
    ItemStack itemstack = null;
    short short1 = datainputstream.readShort();

    if (short1 >= 0) {
      byte b0 = datainputstream.readByte();
      short short2 = datainputstream.readShort();

      itemstack = new ItemStack(short1, b0, short2);
      if ((Item.byId[short1].g()) || (Item.byId[short1].i())) {
        itemstack.tag = c(datainputstream);
      }
    }

    return itemstack;
  }

  protected void a(ItemStack itemstack, DataOutputStream dataoutputstream) throws IOException {
    if ((itemstack == null) || (itemstack.id <= 0)) {
      dataoutputstream.writeShort(-1);
    } else {
      dataoutputstream.writeShort(itemstack.id);
      dataoutputstream.writeByte(itemstack.count);
      dataoutputstream.writeShort(itemstack.getData());
      if ((itemstack.getItem().g()) || (itemstack.getItem().i()))
        a(itemstack.tag, dataoutputstream);
    }
  }

  protected NBTTagCompound c(DataInputStream datainputstream) throws IOException
  {
    short short1 = datainputstream.readShort();

    if (short1 < 0) {
      return null;
    }
    byte[] abyte = new byte[short1];

    datainputstream.readFully(abyte);
    return NBTCompressedStreamTools.a(abyte);
  }

  protected void a(NBTTagCompound nbttagcompound, DataOutputStream dataoutputstream) throws IOException
  {
    if (nbttagcompound == null) {
      dataoutputstream.writeShort(-1);
    } else {
      byte[] abyte = NBTCompressedStreamTools.a(nbttagcompound);

      dataoutputstream.writeShort((short)abyte.length);
      dataoutputstream.write(abyte);
    }
  }

  static {
    a(0, true, true, Packet0KeepAlive.class);
    a(1, true, true, Packet1Login.class);
    a(2, true, true, Packet2Handshake.class);
    a(3, true, true, Packet3Chat.class);
    a(4, true, false, Packet4UpdateTime.class);
    a(5, true, false, Packet5EntityEquipment.class);
    a(6, true, false, Packet6SpawnPosition.class);
    a(7, false, true, Packet7UseEntity.class);
    a(8, true, false, Packet8UpdateHealth.class);
    a(9, true, true, Packet9Respawn.class);
    a(10, true, true, Packet10Flying.class);
    a(11, true, true, Packet11PlayerPosition.class);
    a(12, true, true, Packet12PlayerLook.class);
    a(13, true, true, Packet13PlayerLookMove.class);
    a(14, false, true, Packet14BlockDig.class);
    a(15, false, true, Packet15Place.class);
    a(16, false, true, Packet16BlockItemSwitch.class);
    a(17, true, false, Packet17EntityLocationAction.class);
    a(18, true, true, Packet18ArmAnimation.class);
    a(19, false, true, Packet19EntityAction.class);
    a(20, true, false, Packet20NamedEntitySpawn.class);
    a(21, true, false, Packet21PickupSpawn.class);
    a(22, true, false, Packet22Collect.class);
    a(23, true, false, Packet23VehicleSpawn.class);
    a(24, true, false, Packet24MobSpawn.class);
    a(25, true, false, Packet25EntityPainting.class);
    a(26, true, false, Packet26AddExpOrb.class);
    a(28, true, false, Packet28EntityVelocity.class);
    a(29, true, false, Packet29DestroyEntity.class);
    a(30, true, false, Packet30Entity.class);
    a(31, true, false, Packet31RelEntityMove.class);
    a(32, true, false, Packet32EntityLook.class);
    a(33, true, false, Packet33RelEntityMoveLook.class);
    a(34, true, false, Packet34EntityTeleport.class);
    a(35, true, false, Packet35EntityHeadRotation.class);
    a(38, true, false, Packet38EntityStatus.class);
    a(39, true, false, Packet39AttachEntity.class);
    a(40, true, false, Packet40EntityMetadata.class);
    a(41, true, false, Packet41MobEffect.class);
    a(42, true, false, Packet42RemoveMobEffect.class);
    a(43, true, false, Packet43SetExperience.class);
    a(50, true, false, Packet50PreChunk.class);
    a(51, true, false, Packet51MapChunk.class);
    a(52, true, false, Packet52MultiBlockChange.class);
    a(53, true, false, Packet53BlockChange.class);
    a(54, true, false, Packet54PlayNoteBlock.class);
    a(60, true, false, Packet60Explosion.class);
    a(61, true, false, Packet61WorldEvent.class);
    a(70, true, false, Packet70Bed.class);
    a(71, true, false, Packet71Weather.class);
    a(100, true, false, Packet100OpenWindow.class);
    a(101, true, true, Packet101CloseWindow.class);
    a(102, false, true, Packet102WindowClick.class);
    a(103, true, false, Packet103SetSlot.class);
    a(104, true, false, Packet104WindowItems.class);
    a(105, true, false, Packet105CraftProgressBar.class);
    a(106, true, true, Packet106Transaction.class);
    a(107, true, true, Packet107SetCreativeSlot.class);
    a(108, false, true, Packet108ButtonClick.class);
    a(130, true, true, Packet130UpdateSign.class);
    a(131, true, false, Packet131ItemData.class);
    a(132, true, false, Packet132TileEntityData.class);
    a(200, true, false, Packet200Statistic.class);
    a(201, true, false, Packet201PlayerInfo.class);
    a(202, true, true, Packet202Abilities.class);
    a(250, true, true, Packet250CustomPayload.class);
    a(254, false, true, Packet254GetInfo.class);
    a(255, true, true, Packet255KickDisconnect.class);
  }
}

