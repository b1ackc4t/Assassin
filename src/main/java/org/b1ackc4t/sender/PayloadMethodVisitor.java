package org.b1ackc4t.sender;

import org.objectweb.asm.MethodVisitor;

public class PayloadMethodVisitor extends MethodVisitor {
    private String oldClassName;
    private String newClassName;

    public PayloadMethodVisitor(int api, MethodVisitor methodVisitor, String oldClassName, String newClassName) {
        super(api, methodVisitor);
        this.oldClassName = oldClassName;
        this.newClassName = newClassName;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        if (this.newClassName != null) {
            if (owner.equals(this.oldClassName)) owner = this.newClassName;
        }
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (this.newClassName != null) {
            if (owner.equals(this.oldClassName)) owner = this.newClassName;
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }
}
