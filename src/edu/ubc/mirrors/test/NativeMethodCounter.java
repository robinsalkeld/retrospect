package edu.ubc.mirrors.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.MethodNode;

public class NativeMethodCounter extends ClassVisitor {

    public NativeMethodCounter() {
        super(Opcodes.ASM4);
    }

    int nativeMethodCount = 0;
    String currentClass = null;
    SortedMap<String, Set<MethodNode>> classesWithNativeMethods = new TreeMap<String, Set<MethodNode>>();
    
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
            Set<MethodNode> methods = classesWithNativeMethods.get(currentClass);
            if (methods == null) {
                methods = new HashSet<MethodNode>();
                classesWithNativeMethods.put(currentClass, methods);
            }
            methods.add(new MethodNode(access, name, desc, signature, exceptions));
        }

        return super.visitMethod(access, name, desc, signature, exceptions);
    }
}
