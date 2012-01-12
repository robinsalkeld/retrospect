package edu.ubc.mirrors;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.util.TraceMethodVisitor;

public class MirageClassGenerator extends ClassVisitor {

    public static Type objectMirrorType = Type.getType(ObjectMirror.class);
    public static Type objectMirageType = Type.getType(ObjectMirage.class);
    public static Type fieldMirrorType = Type.getType(FieldMirror.class);
    
    public MirageClassGenerator(ClassVisitor output) {
        super(Opcodes.ASM4, output);
    }
    
    private String superName = null;
    private boolean isInterface;
    
    @Override
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        this.superName = getMirageInternalClassName(superName);
        this.isInterface = (Opcodes.ACC_INTERFACE & access) != 0;
        
        if (!isInterface && superName != null && superName.equals(Type.getInternalName(Object.class))) {
            this.superName = Type.getInternalName(ObjectMirage.class);
        }
        
        String[] mirageInterfaceNames = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            mirageInterfaceNames[i] = getMirageInternalClassName(interfaces[i]);
        }
        
        super.visit(version, access, getMirageInternalClassName(name), signature, this.superName, mirageInterfaceNames);
    }

    public static String getMirageBinaryClassName(String className) {
        if (MirageClassLoader.COMMON_CLASSES.containsKey(className)) {
            return className;
        }
        
        if (className.startsWith("[")) {
            return "[" + getMirageBinaryClassName(className.substring(1));
        }
        
        if (className.startsWith("java") && !className.equals(Object.class.getName())) {
            return "mirage." + className;
        } else {
            return className;
        }
    }
    public static String getMirageInternalClassName(String className) {
        if (className == null) {
            return null;
        }
        
        if (MirageClassLoader.COMMON_CLASSES.containsKey(className.replace('/', '.'))) {
            return className;
        }
        
        Type type = Type.getObjectType(className);
        if (type.getSort() == Type.ARRAY) {
            return "[" + getMirageType(type.getElementType()).getDescriptor();
        }
        
        if (className.startsWith("java") && !className.equals(Type.getInternalName(Object.class))) {
            return "mirage/" + className;
        } else {
            return className;
        }
    }
    
    public static String getMirageMethodDescriptor(String desc) {
        Type methodType = Type.getType(desc);
        
        Type mirageReturnType = getMirageType(methodType.getReturnType());
        Type[] argTypes = methodType.getArgumentTypes();
        Type[] mirageArgTypes = new Type[argTypes.length];
        for (int i = 0; i < argTypes.length; i++) {
            mirageArgTypes[i] = getMirageType(argTypes[i]);
        }
        return Type.getMethodDescriptor(mirageReturnType, mirageArgTypes);
    }
    
    public static String getOriginalClassName(String mirageClassName) {
        if (mirageClassName.startsWith("[")) {
            return "[" + getOriginalClassName(mirageClassName.substring(1));
        }
        
        if (mirageClassName.startsWith("mirage")) {
            return mirageClassName.substring("mirage".length() + 1);
        } else {
            return mirageClassName;
        }
    }
    
    public static Type getMirageType(Type type) {
        if (type.getSort() == Type.OBJECT || type.getSort() == type.ARRAY) {
            return Type.getObjectType(getMirageInternalClassName(type.getInternalName()));
        } else {
            return type;
        }
        
    }
    
    public static String getMirageSignature(String signature) {
        SignatureWriter writer = new MirageSignatureWriter();
        new SignatureReader(signature).accept(writer);
        return writer.toString();
    }
    
    public static String getMirageTypeSignature(String signature) {
        SignatureWriter writer = new MirageSignatureWriter();
        new SignatureReader(signature).acceptType(writer);
        return writer.toString();
    }
    
    private static class MirageSignatureWriter extends SignatureWriter {

        @Override
        public void visitClassType(String name) {
            super.visitClassType(getMirageInternalClassName(name));
        }
    }
    
    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, getMirageInternalClassName(outerName), innerName, access);
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {

        // Handle class renaming
        String mirageDesc = getMirageMethodDescriptor(desc);
        String mirageSig = signature != null ? getMirageSignature(signature) : null;
        String[] mirageExceptions = null;
        if (exceptions != null) {
            mirageExceptions = new String[exceptions.length];
            for (int i = 0; i < exceptions.length; i++) {
                mirageExceptions[i] = getMirageInternalClassName(exceptions[i]);
            }
        }        
        MirageMethodGenerator generator = new MirageMethodGenerator(super.visitMethod(access, name, mirageDesc, mirageSig, mirageExceptions));
        LocalVariablesSorter lvs = new LocalVariablesSorter(access, desc, generator);
        generator.setLocalVariablesSorter(lvs);
        return lvs;
    }
    
    @Override
    public FieldVisitor visitField(int access, String name, String desc,
            String signature, Object value) {
        
        // Remove all field definitions
        return null;
    }

    @Override
    public void visitEnd() {
        // Generate the constructor that takes a mirror instance
        if (!isInterface && superName != null) {
            String constructorDesc = Type.getMethodDescriptor(Type.VOID_TYPE, objectMirrorType);
            MethodVisitor methodVisitor = super.visitMethod(Opcodes.ACC_PUBLIC, 
                             "<init>", constructorDesc, null, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>", constructorDesc);
            methodVisitor.visitInsn(Opcodes.RETURN);
            methodVisitor.visitMaxs(2, 2);
            methodVisitor.visitEnd();
        }
        
        super.visitEnd();
    }
    
    public static byte[] generate(String className, ClassReader reader, String traceDir) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES & ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = classWriter;
        if (traceDir != null) {
            try {
                PrintWriter pw = new PrintWriter(traceDir + className + ".txt");
                visitor = new TraceClassVisitor(visitor, pw);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        visitor = new CheckClassAdapter(visitor);
        visitor = new MirageClassGenerator(visitor);
        reader.accept(visitor, ClassReader.SKIP_FRAMES);
        return classWriter.toByteArray();
    }
    
    private static class Tracer extends ClassVisitor {

        private final PrintWriter pw;
        private Printer p;
        
        public Tracer(ClassVisitor cv, PrintWriter writer) {
            super(Opcodes.ASM4, cv);
            this.pw = writer;
            this.p = new Textifier();
        }
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                String signature, String[] exceptions) {
            MethodVisitor result = super.visitMethod(access, name, desc, signature, exceptions);
            return result;
        }
        
        
        @Override
        public void visitEnd() {
            super.visitEnd();
            p.print(pw);
            pw.flush();
        }
    }
}
