package edu.ubc.mirrors.test;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class NativeMethodCounter extends ClassVisitor {

    public NativeMethodCounter() {
        super(Opcodes.ASM4);
    }

    int nativeMethodCount = 0;
    String currentClass = null;
    Set<String> classesWithNativeMethods = new TreeSet<String>();
    
    @Override
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        currentClass = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
        
        if ((access & Opcodes.ACC_NATIVE) != 0) {
            nativeMethodCount++;
            classesWithNativeMethods.add(currentClass);
        }

        return super.visitMethod(access, name, desc, signature, exceptions);
    }
}
