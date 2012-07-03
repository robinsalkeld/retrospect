package edu.ubc.mirrors.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
    Map<String, List<String>> classesWithNativeMethods = new TreeMap<String, List<String>>();
    
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
            List<String> methods = classesWithNativeMethods.get(currentClass);
            if (methods == null) {
                methods = new ArrayList<String>();
                classesWithNativeMethods.put(currentClass, methods);
            }
            methods.add(name);
        }

        return super.visitMethod(access, name, desc, signature, exceptions);
    }
}
