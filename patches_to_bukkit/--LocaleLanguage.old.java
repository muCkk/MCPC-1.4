package net.minecraft.server;

import cpw.mods.fml.common.FMLCommonHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeMap;

public class LocaleLanguage
{
  private static LocaleLanguage a = new LocaleLanguage();
  private Properties b = new Properties();
  private TreeMap c;
  private String d;
  private boolean e;

  private LocaleLanguage()
  {
    b();
    a("en_US");
  }

  public static LocaleLanguage a() {
    return a;
  }

  private void b() {
    TreeMap treemap = new TreeMap();
    try
    {
      BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(LocaleLanguage.class.getResourceAsStream("/lang/languages.txt"), "UTF-8"));

      for (String s = bufferedreader.readLine(); s != null; s = bufferedreader.readLine()) {
        String[] astring = s.split("=");

        if ((astring != null) && (astring.length == 2))
          treemap.put(astring[0], astring[1]);
      }
    }
    catch (IOException ioexception) {
      ioexception.printStackTrace();
      return;
    }

    this.c = treemap;
  }

  private void a(Properties properties, String s) throws IOException {
    BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(LocaleLanguage.class.getResourceAsStream("/lang/" + s + ".lang"), "UTF-8"));

    for (String s1 = bufferedreader.readLine(); s1 != null; s1 = bufferedreader.readLine()) {
      s1 = s1.trim();
      if (!s1.startsWith("#")) {
        String[] astring = s1.split("=");

        if ((astring != null) && (astring.length == 2)) {
          properties.setProperty(astring[0], astring[1]);
        }
      }
    }
    FMLCommonHandler.instance().handleLanguageLoad(properties, s);
  }

  public void a(String s) {
    if (!s.equals(this.d)) {
      Properties properties = new Properties();
      try
      {
        a(properties, "en_US");
      }
      catch (IOException ioexception)
      {
      }
      this.e = false;
      if (!"en_US".equals(s)) {
        try {
          a(properties, s);
          Enumeration enumeration = properties.propertyNames();

          while ((enumeration.hasMoreElements()) && (!this.e)) {
            Object object = enumeration.nextElement();
            Object object1 = properties.get(object);

            if (object1 != null) {
              String s1 = object1.toString();

              for (int i = 0; i < s1.length(); i++)
                if (s1.charAt(i) >= 'Ā') {
                  this.e = true;
                  break;
                }
            }
          }
        }
        catch (IOException ioexception1) {
          ioexception1.printStackTrace();
          return;
        }
      }

      this.d = s;
      this.b = properties;
    }
  }

  public String b(String s) {
    return this.b.getProperty(s, s);
  }

  public String a(String s, Object[] aobject) {
    String s1 = this.b.getProperty(s, s);

    return String.format(s1, aobject);
  }

  public Properties getCurrentLanguageTable() {
    return this.b;
  }

  public String getCurrentLanguage() {
    return this.d;
  }
}

