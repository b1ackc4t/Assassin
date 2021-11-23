package org.b1ackc4t.util;

import java.util.HashMap;
import java.util.Map;

import static java.lang.System.arraycopy;
import java.util.regex.*;

public class Command {
    private String cmd;
    private String[] args;
    private Map<String, String> kwargs;
    private String raw;

    public Command(String s) {
        raw = new String(s);
        kwargs = new HashMap<>();
        Pattern p = Pattern.compile("(\\w+)\\s*=\\s*(\\S+|\"\\S*?\")");
        Matcher m = p.matcher(raw);
        while (m.find()) {
            kwargs.put(m.group(1).toLowerCase(), m.group(2).replace("\"", ""));
        }
        s = s.replaceAll("(\\w+)\\s*=\\s*(\\S+|\"\\S*?\")", "");
        String[] cmdArgs = s.split("\\s+");
        if (cmdArgs.length > 0) {
            char[] cs = cmdArgs[0].toLowerCase().toCharArray();
            if (cs[0] >= 97 && cs[0] <= 122) {
                cs[0] -= 32;
            }
            this.cmd = String.valueOf(cs);
            if (cmdArgs.length > 1) {
                args = new String[cmdArgs.length - 1];
                arraycopy(cmdArgs, 1, args, 0, cmdArgs.length - 1);

            }
        }
    }

    public String getArg(int index) {
        return (index >= 0 && index < this.args.length) ? this.args[index] : "";
    }

    public String[] getArgs() { return this.args; }

    public int getArgNum() {
        return this.args.length;
    }

    public String getCmd() {
        return this.cmd;
    }

    public String getRaw() {
        return this.raw;
    }

    public Map getKwargs() { return this.kwargs; }
}
