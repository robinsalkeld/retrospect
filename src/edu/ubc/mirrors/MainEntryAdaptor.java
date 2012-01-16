package edu.ubc.mirrors;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

public class MainEntryAdaptor extends ClassVisitor {

    public MainEntryAdaptor(ClassVisitor output) {
        super(Opcodes.ASM4, output);
    }
    
    private String className;
    
    private static final String mainDesc = Type.getMethodDescriptor(
            Type.getType(Void.TYPE), 
            Type.getType(String[].class));
    
    private static final String invokeMirageMainMethodDesc = Type.getMethodDescriptor(
            Type.getType(Void.TYPE), 
            Type.getType(Class.class), Type.getType(String[].class));
    
    @Override
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        
        this.className = name;
        
        super.visit(version, access, name, signature, superName, interfaces);
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
        
        MethodVisitor superVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        if (superVisitor != null && name.equals("main") && (Opcodes.ACC_STATIC & access) != 0 && desc.equals(mainDesc)) {
            superVisitor.visitCode();
            superVisitor.visitLdcInsn(Type.getObjectType(className));
            superVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            superVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ObjectMirage.class), "invokeMirageMainMethod", invokeMirageMainMethodDesc);
            superVisitor.visitInsn(Opcodes.RETURN);
            superVisitor.visitMaxs(2, 1);
            superVisitor.visitEnd();
            return null;
        } else {
            return superVisitor;
        }
    }
    
    public static byte[] generate(String className, ClassReader reader, String traceDir) throws FileNotFoundException {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES & ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = classWriter;
        if (traceDir != null) {
            visitor = new TraceClassVisitor(visitor, new PrintWriter(traceDir + className + ".txt"));
        }
        visitor = new CheckClassAdapter(visitor);
        visitor = new MainEntryAdaptor(visitor);
        reader.accept(visitor, ClassReader.SKIP_FRAMES);
        return classWriter.toByteArray();
    }
    
    
}
