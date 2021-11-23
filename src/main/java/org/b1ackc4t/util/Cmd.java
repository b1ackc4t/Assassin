package org.b1ackc4t.util;


import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;

public class Cmd {

    public String prompt;
    private InputStream stdin;
    private OutputStream stdout;
    private Queue cmdQueue;

    public Cmd(InputStream stdin, OutputStream stdout) {
        this.stdin = stdin;
        this.stdout = stdout;
        this.prompt = "(cmd) ";
        this.cmdQueue = new LinkedList<Object>();

    }

    public Cmd() {
        this.stdin = System.in;
        this.stdout = System.out;
        this.prompt = "(cmd) ";
        this.cmdQueue = new LinkedList<Object>();
    }

    // 开启循环之前调用
    private void preLoop() {

    }

    private void postLoop() {

    }

    // 每次循环前调用
    public boolean preCmd(Command command) {
        return false;
    }

    private boolean oneCmd(Command command) {

        boolean stop = false;
        try {
            Method m = this.getClass().getMethod("do" + command.getCmd(), String[].class, Map.class);
            stop = (boolean) m.invoke(this, command.getArgs(), command.getKwargs());
        } catch (NoSuchMethodException e) {
            System.out.println(command.getCmd() + ": command not found");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return stop;
    }

    private void postCmd(Command command) {

    }

    public void cmdLoop() {
        preLoop();
        boolean stop = false;
        boolean jmp = false;
        Scanner sc = new Scanner(System.in);
        while (!stop) {
            System.out.format(Config.getPColor("%s"), prompt);
            String temp_s = sc.nextLine();
            if (temp_s.equals("")) continue;
            Command command = new Command(temp_s);
            jmp = preCmd(command);
            if (jmp) continue;
            stop = oneCmd(command);
            postCmd(command);
        }
        postLoop();

    }

    public boolean doHelp(String[] args, Map<String, String> kwargs) {
        System.out.println("[command] [options]");
        return false;
    }

    public boolean doExit(String[] args, Map<String, String> kwargs) {
        return true;
    }

}
