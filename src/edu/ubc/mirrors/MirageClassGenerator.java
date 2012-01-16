package edu.ubc.mirrors;

import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP2;
import static org.objectweb.asm.Opcodes.RETURN;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureWriter;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

public class MirageClassGenerator extends RemappingClassAdapter {

    public static Type objectMirrorType = Type.getType(ObjectMirror.class);
    public static Type objectMirageType = Type.getType(ObjectMirage.class);
    public static Type fieldMirrorType = Type.getType(FieldMirror.class);
    public static Type nativeObjectMirrorType = Type.getType(NativeObjectMirror.class);
    
    public static Remapper REMAPPER = new Remapper() {
        public String map(String typeName) {
            return getMirageInternalClassName(typeName);
        };
    };
    
    public MirageClassGenerator(ClassVisitor output) {
        super(output, REMAPPER);
    }
    
    private String superName = null;
    private boolean isInterface;
    
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.isInterface = (Opcodes.ACC_INTERFACE & access) != 0;
        this.superName = superName;
        
        if (!isInterface && Type.getInternalName(Object.class).equals(superName)) {
            this.superName = Type.getInternalName(ObjectMirage.class);
        } else {
            this.superName = getMirageInternalClassName(this.superName);
        }
        
        super.visit(version, access, name, signature, this.superName, interfaces);
    }

    public static String getMirageBinaryClassName(String className) {
        if (className == null) {
            return null;
        }
        
        return getMirageInternalClassName(className.replace('.', '/')).replace('/', '.');
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
        
        if (!className.startsWith("mirage") || className.startsWith("java")) {
            return "mirage/" + className;
        } else {
            return className;
        }
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
        if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY) {
            return Type.getObjectType(getMirageInternalClassName(type.getInternalName()));
        } else {
            return type;
        }
        
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {

//        if (false && name.equals("main")) {
//            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
//            mv.visitCode();
//            Label l0 = new Label();
//            mv.visitLabel(l0);
//            mv.visitLineNumber(17, l0);
//            mv.visitTypeInsn(NEW, "examples/MirageTest");
////            mv.visitInsn(DUP);
////            mv.visitTypeInsn(NEW, "edu/ubc/mirrors/NativeObjectMirror");
////            mv.visitInsn(DUP);
//            mv.visitTypeInsn(NEW, "mirage/examples/MirageTest");
////            mv.visitMethodInsn(INVOKESPECIAL, "edu/ubc/mirrors/NativeObjectMirror", "<init>", "(Ljava/lang/Object;)V");
////            mv.visitInsn(POP2);
//            mv.visitInsn(DUP);
//            mv.visitInsn(DUP);
//            mv.visitInsn(DUP);
//            mv.visitLdcInsn(Type.getType("Lmirage/examples/Bar;"));
//            mv.visitMethodInsn(INVOKESPECIAL, "examples/MirageTest", "<init>", "(Ljava/lang/Class;)V");
//            Label l1 = new Label();
//            mv.visitLabel(l1);
//            mv.visitLineNumber(22, l1);
//            mv.visitInsn(RETURN);
//            Label l2 = new Label();
//            mv.visitLabel(l2);
//            mv.visitLocalVariable("args", "[Ljava/lang/String;", null, l0, l2, 0);
//            mv.visitMaxs(12, 1);
//            mv.visitEnd();
//            return null;
//        }
        
        // toString() is a special case - it's defined in java.lang.Object, which this class must ultimately
        // extend, so we have to return a real String rather than a mirage.
        boolean isToString = (name.equals("toString") && desc.equals(Type.getMethodType(Type.getType(String.class))));
        
        MirageMethodGenerator generator = new MirageMethodGenerator(super.visitMethod(access, name, desc, signature, exceptions), isToString);
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
        visitor = new CheckClassAdapter(visitor);
        visitor = new MirageClassGenerator(visitor);
        reader.accept(visitor, ClassReader.SKIP_FRAMES);
        
        if (traceDir != null) {
            try {
                String fileName = traceDir + className + ".class";
                OutputStream classFile = new FileOutputStream(fileName);
                classFile.write(classWriter.toByteArray());
                classFile.close();
            } catch (IOException e) {
                // ignore
            }
        }
        
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
