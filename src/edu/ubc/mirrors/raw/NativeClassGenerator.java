package edu.ubc.mirrors.raw;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.util.CheckClassAdapter;

import edu.ubc.mirrors.mirages.MirageClassLoader;

public class NativeClassGenerator extends RemappingClassAdapter {

    public static Remapper REMAPPER = new Remapper() {
        public String map(String typeName) {
            return getNativeInternalClassName(typeName);
        };  
    };
    
    public NativeClassGenerator(ClassVisitor cv) {
        super(cv, REMAPPER);
    }
    
    private String className;
    private String superName;
    private boolean isInterface;
    
    @Override
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {

        this.className = getNativeInternalClassName(name);
        this.superName = getNativeInternalClassName(superName);
        this.isInterface = (Opcodes.ACC_INTERFACE & access) != 0;
        
        super.visit(version, access, this.className, signature, this.superName, interfaces);
    }
    
    public static String getNativeBinaryClassName(String className) {
        if (className == null) {
            return null;
        }
        
        return getNativeInternalClassName(className.replace('.', '/')).replace('/', '.');
    }
    public static String getNativeInternalClassName(String className) {
        if (className == null) {
            return null;
        }
        
        if (MirageClassLoader.COMMON_CLASSES.containsKey(className.replace('/', '.'))) {
            return className;
        }
        
        if (className.equals(Type.getInternalName(Object.class))) {
            return className;
        }
        
        if (!className.startsWith("native") || className.startsWith("java")) {
            return "native/" + className;
        } else {
            return className;
        }
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
        
        return null;
    }
    
    @Override
    public void visitEnd() {
        if (!isInterface) {
            // Generate the no-argument constructor
            String constructorDesc = Type.getMethodDescriptor(Type.VOID_TYPE);
            MethodVisitor methodVisitor = super.visitMethod(Opcodes.ACC_PUBLIC, 
                             "<init>", constructorDesc, null, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>", constructorDesc);
            methodVisitor.visitInsn(Opcodes.RETURN);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd(); 
        }
        
        super.visitEnd();
    }
    
    public static String getOriginalClassName(String nativeClassName) {
        if (nativeClassName.startsWith("[")) {
            return "[" + getOriginalClassName(nativeClassName.substring(1));
        }
        
        if (nativeClassName.startsWith("native")) {
            return nativeClassName.substring("native".length() + 1);
        } else {
            return nativeClassName;
        }
    }
    
    public static byte[] generate(String className, ClassReader reader) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES & ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = classWriter;
        visitor = new CheckClassAdapter(visitor);
        visitor = new NativeClassGenerator(visitor);
        reader.accept(visitor, ClassReader.SKIP_FRAMES);
        return classWriter.toByteArray();
    }
}
