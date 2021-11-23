package org.b1ackc4t.util;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import java.util.Map;
import java.util.Random;

import com.alibaba.fastjson.JSONObject;
import org.b1ackc4t.sender.Crypt;


public class Utils {
    public static String byteToUrlEncode(byte[] data) {
        StringBuffer sb = new StringBuffer();
        for (byte b : data) {
            String tmp = Integer.toHexString(b & 0xFF);
//            if (b < 0) tmp = tmp.substring(tmp.length() - 2);
            sb.append(String.format("%%%2s", tmp).replace(' ', '0'));
        }
        return sb.toString();
    }

    /**
     * 首字母大写 其他小写
     * @param str
     * @return
     */
    public static String captureName(String str) {
        char[] cs=str.toLowerCase().toCharArray();
        cs[0]-=32;
        return String.valueOf(cs);
    }

    public static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < length; ++i) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    public static String getRandomAlpha(int length) {
        String str = "abcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < length; ++i) {
            int number = random.nextInt(26);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 解析headers的set-cookie得到cookie值
     * @param headers
     * @return
     */
    public static String getCookieBySetCookie(Map headers) {
        Iterator it = headers.keySet().iterator();
        String setcookie = null;
        StringBuffer sb = new StringBuffer();
        String[] cookieProperty = new String[]{"expires", "max-age", "domain", "path", "secure", "httponly", "samesite"};
        while (it.hasNext()) {
            String key = (String) it.next();
            if (key != null && key.equalsIgnoreCase("Set-Cookie")) {
                setcookie = (String) headers.get(key);
                String[] cookiePairs = setcookie.split(";");
                for(int i = 0; i < cookiePairs.length; ++i) {
                    Set cookiePropertyList = new HashSet(Arrays.asList(cookieProperty));
                    String[] cookiePair = cookiePairs[i].split("=");
                    if (cookiePair.length > 1) {
                        String cookieKey = cookiePair[0];
                        if (!cookiePropertyList.contains(cookieKey.toLowerCase().trim())) {
                            sb.append(cookiePairs[i]);
                            sb.append(";");
                        }
                    }
                }
            }
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
            return sb.toString();
        } else {
            return null;
        }
    }

    /**
     * 将url参数转换成map
     *
     * @param param aa=11&bb=22&cc=33
     * @return
     */
    public static Map<String, Object> getUrlParams(String param) {
        Map<String, Object> map = new HashMap<String, Object>(0);
        if (param == null) {
            return map;
        }
        String[] params = param.split("&");
        for (int i = 0; i < params.length; i++) {
            String[] p = params[i].split("=");
            if (p.length == 2) {
                map.put(p[0], p[1]);
            }
        }
        return map;
    }

    public static String getRandomItem(List<String> l) {
        Random random = new Random();
        int n = 0;
        if (l == null || l.size() == 0) return null;
        try {
            n = random.nextInt(l.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return l.get(n);
    }

    /**
     * 将map转换成url参数
     *
     * @param map
     * @return
     */
    public static String getUrlParamsByMap(Map<String, String> map) {
        if (map == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey() + "=" + entry.getValue());
            sb.append("&");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public static String getCookieByMap(Map<String, String> map) {
        if (map == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey() + "=" + entry.getValue());
            sb.append(";");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public static Proxy parseProxyString(String add) {
        if (add == null) {
            return Proxy.NO_PROXY;
        }
        String[] a = add.split(":");
        InetSocketAddress addr = new InetSocketAddress(a[0], Integer.parseInt(a[1]));
        return new Proxy(Proxy.Type.HTTP, addr);
    }

    /**
     * 发送get请求
     * @param httpUrl
     * @param headers header头
     * @param params get参数
     * @return
     */
    public static Map<String, Object> sendGetRequest(String httpUrl, Map<String, String> headers, Map<String, String> params, String proxy, String auth) {
        HttpURLConnection connection = null;
        InputStream is = null;
        DataInputStream dis = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int length;
        Map<String, Object> result = new HashMap<>();

        try {
            String paramsStr = getUrlParamsByMap(params);
            String realUrl = !paramsStr.equals("") ? String.format("%s?%s", httpUrl, paramsStr) : httpUrl;
            URL url = new URL(realUrl);

            connection = (HttpURLConnection) url.openConnection(parseProxyString(proxy));
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setConnectTimeout(12000);
            connection.setReadTimeout(12000);
            if (auth != null) {
                String a = "Basic " + new String(Crypt.b64Encoder.encode(auth.getBytes())); //帐号密码用:隔开，base64加密方式
                connection.setRequestProperty("Proxy-Authorization", a);
            }
            if (headers != null) {
                Set<String> keys = headers.keySet();
                for(String key : keys) {
                    connection.setRequestProperty(key, headers.get(key));
                }
            }
            connection.connect();
            is = connection.getInputStream();
            if (null != is) {
                dis = new DataInputStream(is);
                byte[] tmp = new byte[1024];
                while ((length = dis.read(tmp)) != -1) {
                    baos.write(tmp, 0, length);
                }
            }
            result.put("data", baos.toByteArray());
            result.put("status", connection.getResponseCode());
            Map<String, String> responseHeaders = new HashMap<>();
            Iterator<String> it = connection.getHeaderFields().keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                responseHeaders.put(key, connection.getHeaderField(key));
            }
            result.put("headers", responseHeaders);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != dis) {
                    dis.close();
                }
                if (null != is) {
                    is.close();
                }
                    baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (null != connection) {
                connection.disconnect();
            }
        }

        return null;
    }

    public static Map<String, Object> sendPostRequest(String httpUrl, Map<String, String> headers, Map<String, String> params) {
        HttpURLConnection connection = null;
        InputStream is = null;
        DataInputStream dis = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int length;
        Map<String, Object> result = new HashMap<>();

        try {
            URL url = new URL(httpUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setConnectTimeout(12000);
            connection.setReadTimeout(12000);
            if (headers != null) {
                Set<String> keys = headers.keySet();
                for(String key : keys) {
                    connection.setRequestProperty(key, headers.get(key));
                }
            }
            connection.setDoOutput(true);
            connection.setDoInput(true);
            OutputStream outwritestream = connection.getOutputStream();
            outwritestream.write(getUrlParamsByMap(params).getBytes());
            outwritestream.flush();
            outwritestream.close();
            connection.connect();
            is = connection.getInputStream();
            if (null != is) {
                dis = new DataInputStream(is);
                byte[] tmp = new byte[1024];
                while ((length = dis.read(tmp)) != -1) {
                    baos.write(tmp, 0, length);
                }
            }
            result.put("data", baos.toByteArray());
            result.put("status", connection.getResponseCode());
            Map<String, String> responseHeaders = new HashMap<>();
            Iterator<String> it = connection.getHeaderFields().keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                responseHeaders.put(key, connection.getHeaderField(key));
            }
            result.put("headers", responseHeaders);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != dis) {
                    dis.close();
                }
                if (null != is) {
                    is.close();
                }
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (null != connection) {
                connection.disconnect();
            }
        }

        return null;
    }

    /**
     * @description 获取文件的内容
     * @param local_path 本地文件路径
     * @return byte[] 文件内容的字节数组
     */
    public static byte[] getFileContent(String local_path) {
        try {

            File file = new File(local_path);

            int fileSize = (int)file.length();
            byte[] buffer = new byte[fileSize];
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(buffer,0,fileSize);
            fileInputStream.close();
            return buffer;

        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }

    public static String getFileName(String path) {
        String name;
        name = path.substring(path.lastIndexOf("/")+1);
        name = name.substring(name.lastIndexOf("\\")+1);
        return name;
    }

    public static String readJsonFile(File jsonFile) {
        String jsonStr = "";
        try {
            FileReader fileReader = new FileReader(jsonFile);
            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), StandardCharsets.UTF_8);
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> readFileByLines(String fileName) {
        File file = new File(fileName);
        List<String> list = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                list.add(tempString);
            }
            reader.close();
            return list;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return null;
    }

    public static Map<String, Object> jsonToMap(JSONObject json) {
        Map<String, Object> map = new HashMap<String, Object>();
        //最外层解析
        for(Object k : json.keySet()){
            Object v = json.get((String) k);
            map.put(k.toString(), v);
        }
        return map;
    }

    /**
     * 拆分byte数组
     *
     * @param bytes
     * @param num
     * @return
     */
    public static byte[][] splitBytes(byte[] bytes, int num) {
        int partLength = (int) Math.ceil(bytes.length / ((float)num));
        byte[][] result = new byte[num][];
        int from, to;
        for (int i = 0; i < num; i++) {

            from = (int) (i * partLength);
            to = (int) (from + partLength);
            if (to > bytes.length)
                to = bytes.length;
            result[i] = Arrays.copyOfRange(bytes, from, to);
        }
        return result;
    }

    /**
     * 写字节到文件
     * @param target
     * @param src
     * @return
     */
    public static boolean writeFileByBytes(File target, byte[] src) {
        OutputStream fos = null;
        try {
            if (!target.exists()) createDirAndFile(target);
            fos = new FileOutputStream(target);
            fos.write(src);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean createDirAndFile(File target) {
        File parent = target.getParentFile();
        try {
            if (parent != null && !parent.exists()) {
                if(!parent.mkdirs()) throw new IOException("Folder creation failed.");
            }
            return target.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String readFileByString(File f) {
        Reader fis = null;
        BufferedReader br = null;
        Writer sw = new StringWriter();
        try {
            fis = new FileReader(f);
            br = new BufferedReader(fis);
            char[] tmp = new char[4096];
            int len;
            while ((len = br.read(tmp)) != -1) {
                sw.write(tmp, 0, len);
            }
            return sw.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                sw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static boolean writeFileByString(File target, String src) {
        Writer w = null;
        BufferedWriter bw = null;
        try {
            if (!target.exists()) createDirAndFile(target);
            w = new FileWriter(target);
            bw = new BufferedWriter(w);
            bw.write(src);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (w != null) {
                try {
                    w.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}
