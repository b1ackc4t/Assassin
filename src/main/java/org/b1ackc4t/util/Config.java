package org.b1ackc4t.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


import java.io.File;
import java.util.*;

public class Config {
    public static Map<String, String> headers = new HashMap<>();
    public static List<String> paramNames;
    public static List<String> cookieNames;
    public static int partNum;
    public static int argNum;
    public static boolean startRandomUserAgent;
    public static boolean startIpAgents;
    public static boolean startColor;

    static {
        JSONObject config = null;
        try {
            config = JSON.parseObject(Utils.readJsonFile(new File("config.json")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (config != null) {
            if (config.get("headers") != null) {
                headers.putAll((Map<String, String>) config.get("headers"));
            }
            if (config.get("paramNames") != null) {
                paramNames = (List<String>) config.get("paramNames");
            } else {
                paramNames = Arrays.asList("user", "file", "id", "eid", "wd", "ie", "oq", "name", "son");
            }

            if (config.get("cookieNames") != null) {
                cookieNames = (List<String>) config.get("cookieNames");
            } else {
                cookieNames = Arrays.asList("fid", "uuid", "eid", "home", "ief", "fl", "oop");
            }

            partNum = config.get("partNum") != null ? config.getIntValue("partNum") : 3;
            argNum = config.get("argNum") != null ? config.getIntValue("argNum") : 5;
            startRandomUserAgent = config.get("startRandomUserAgent") != null ? config.getBoolean("startRandomUserAgent") : false;
            startIpAgents = config.get("startIpAgents") != null ? config.getBoolean("startIpAgents") : false;
            startColor = config.get("startColor") != null ? config.getBoolean("startColor") : true;
        }

    }

    public static String getYColor(String s) {
        if (startColor) {
            return "\33[33;1m" + s + "\33[0m";
        }
        return s;
    }

    public static String getPColor(String s) {
        if (startColor) {
            return "\33[35;1m" + s + "\33[0m";
        }
        return s;
    }
}
