package org.b1ackc4t.sender;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PayloadClassVisitor extends ClassVisitor {
    private String oldClassName;
    private String newClassName;
    private Map<String, Object> fieldsMap;
    private int javaVersion;
    private Set<String> existed;

    public PayloadClassVisitor(int api, ClassVisitor classVisitor, Map fieldsMap) {
        super(api, classVisitor);
        this.fieldsMap = fieldsMap;
        this.javaVersion = -1;
        this.existed = new HashSet<>();
    }

    public void setNewClassName(String newClassName) {this.newClassName = newClassName;}

    public void setJavaVersion(int javaVersion) {this.javaVersion = javaVersion;}

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (this.oldClassName == null) this.oldClassName = name;
        if (this.javaVersion != -1) version = this.javaVersion;
        if (this.newClassName != null) name = this.newClassName;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if (fieldsMap.containsKey(name)) {
            value = fieldsMap.get(name);
            existed.add(name);
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (this.newClassName != null) {
            return new PayloadMethodVisitor(this.api, mv, this.oldClassName, this.newClassName);
        }
        return mv;
    }

    @Override
    public void visitEnd() {
        Set<String> needAdd = new HashSet<>(this.fieldsMap.keySet());
        needAdd.removeAll(existed);
        for (String name : needAdd) {
            super.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    name,
                    "Ljava/lang/String;",
                    null,
                    this.fieldsMap.get(name)
            );
        }
        super.visitEnd();
    }
}
