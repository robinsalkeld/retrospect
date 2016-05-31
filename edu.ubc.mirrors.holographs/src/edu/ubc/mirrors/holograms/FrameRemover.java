package edu.ubc.mirrors.holograms;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class FrameRemover extends ClassVisitor {

    public FrameRemover(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new MethodFrameRemover(super.visitMethod(access, name, desc, signature, exceptions));
    }
    
    private static class MethodFrameRemover extends MethodVisitor {

        public MethodFrameRemover(MethodVisitor mv) {
            super(Opcodes.ASM5, mv);
        }
        
        @Override
        public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
            // Whoops, I think I dropped something...
        }
    }
    
}
