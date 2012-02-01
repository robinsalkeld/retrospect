package edu.ubc.mirrors;

import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Properties;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.util.CheckClassAdapter;

public class MirageClassGenerator extends ClassVisitor {

    public static Type objectMirrorType = Type.getType(ObjectMirror.class);
    public static Type objectMirageType = Type.getType(ObjectMirage.class);
    public static Type fieldMirrorType = Type.getType(FieldMirror.class);
    public static Type nativeObjectMirrorType = Type.getType(NativeObjectMirror.class);
    public static Type fieldMapMirrorType = Type.getType(FieldMapMirror.class);
    public static Type classType = Type.getType(Class.class);
    
    public static Remapper REMAPPER = new Remapper() {
        public String map(String typeName) {
            return getMirageInternalClassName(typeName);
        };
    };
    
    public MirageClassGenerator(ClassVisitor output) {
        super(Opcodes.ASM4, output);
    }
    
    private String name = null;
    private String superName = null;
    private boolean isInterface;
    
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.name = name;
        this.isInterface = (Opcodes.ACC_INTERFACE & access) != 0;
        this.superName = superName;
        
        if (!isInterface && Type.getInternalName(Object.class).equals(superName)) {
            this.superName = Type.getInternalName(ObjectMirage.class);
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
    
    public static Type getMirageType(Class<?> c) {
        return getMirageType(Type.getType(c));
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {

        MethodVisitor superVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        
        
        // toString() is a special case - it's defined in java.lang.Object, which this class must ultimately
        // extend, so we have to return a real String rather than a mirage.
        boolean isToString = (name.equals("toString") && desc.equals(Type.getMethodType(Type.getType(String.class))));
        
        MirageMethodGenerator generator = new MirageMethodGenerator(superVisitor, superName, Type.getMethodType(desc), isToString);
        LocalVariablesSorter lvs = new LocalVariablesSorter(access, desc, generator);
        generator.setLocalVariablesSorter(lvs);
        
        if ((Opcodes.ACC_NATIVE & access) != 0) {
            String systemName = Type.getInternalName(System.class);
            String mirageSystemName = getMirageInternalClassName(systemName);
            if (this.name.equals(mirageSystemName) && name.equals("registerNatives")) {
                generator.visitCode();
                generator.visitInsn(Opcodes.RETURN);
                generator.visitMaxs(0, 0);
                
                return null;
            } else if (this.name.equals(mirageSystemName) && name.equals("setIn0")) {
                generator.visitCode();
                generator.visitVarInsn(Opcodes.ALOAD, 0);
                generator.visitFieldInsn(Opcodes.PUTSTATIC, mirageSystemName, "in", Type.getDescriptor(InputStream.class));
                generator.visitMaxs(1, 1);
                
                return null;
            } else if (this.name.equals(mirageSystemName) && name.equals("setOut0")) {
                generator.visitCode();
                generator.visitVarInsn(Opcodes.ALOAD, 0);
                generator.visitFieldInsn(Opcodes.PUTSTATIC, mirageSystemName, "out", Type.getDescriptor(PrintStream.class));
                generator.visitMaxs(1, 1);
                
                return null;
            } else if (this.name.equals(mirageSystemName) && name.equals("setErr0")) {
                generator.visitCode();
                generator.visitVarInsn(Opcodes.ALOAD, 0);
                generator.visitFieldInsn(Opcodes.PUTSTATIC, mirageSystemName, "err", Type.getDescriptor(PrintStream.class));
                generator.visitMaxs(1, 1);
                
                return null;
            } else if (this.name.equals(mirageSystemName) && (name.equals("currentTimeMillis") || name.equals("nanoTime"))) {
                generator.visitCode();
                generator.visitMethodInsn(Opcodes.INVOKESTATIC, systemName, name, Type.getMethodDescriptor(Type.LONG_TYPE));
                generator.visitMaxs(1, 1);
                
                return null;
            } else if (this.name.equals(mirageSystemName) && (name.equals("identityHashCode"))) {
                // TODO-RS: Not correct for mirages!
                generator.visitCode();
                generator.load(0, Type.getType(Object.class));
                generator.visitMethodInsn(Opcodes.INVOKESTATIC, systemName, name, Type.getMethodDescriptor(Type.INT_TYPE));
                generator.visitMaxs(1, 1);
                
                return null;
            } else if (this.name.equals(mirageSystemName) && (name.equals("arraycopy"))) {
                // TODO-RS: Not correct for mirages!
                generator.visitCode();
                generator.visitVarInsn(Opcodes.ALOAD, 0);
                generator.visitVarInsn(Opcodes.ILOAD, 1);
                generator.visitVarInsn(Opcodes.ALOAD, 2);
                generator.visitVarInsn(Opcodes.ILOAD, 3);
                generator.visitVarInsn(Opcodes.ILOAD, 4);
                generator.visitMethodInsn(Opcodes.INVOKESTATIC, systemName, name, 
                        Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class), Type.INT_TYPE, Type.getType(Object.class), Type.INT_TYPE, Type.INT_TYPE));
                generator.visitInsn(Opcodes.RETURN);
                generator.visitMaxs(5, 5);
                
                return null;
            } else if (this.name.equals(mirageSystemName) && (name.equals("initProperties"))) {
                generator.visitCode();
                generator.putstatic(mirageSystemName, "props", getMirageType(Properties.class).getDescriptor());
                generator.areturn(Type.VOID_TYPE);
                generator.visitMaxs(1, 1);
                
                return null;
            } else if (this.name.equals(mirageSystemName) && (name.equals("mapLibraryName"))) {
                generateStaticThunk(generator, System.class, name, String.class);
                
                return null;
            } else {
                throw new UnsupportedOperationException("Unsupported native method: " + this.name + "#" + name);
            }
        }
        
        return lvs;
    }
    
    public static void generateStaticThunk(MethodVisitor visitor, Class<?> klass, String methodName, Class<?> ... parameterClasses) {
        Method method;
        try {
            method = klass.getMethod(methodName, parameterClasses);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        visitor.visitCode();
        Type[] parameterTypes = new Type[parameterClasses.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = Type.getType(parameterClasses[i]);
            visitor.visitVarInsn(parameterTypes[i].getOpcode(Opcodes.ILOAD), i);
        }
        Type returnType = Type.getType(method.getReturnType());
        visitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(klass), methodName, Type.getMethodDescriptor(returnType, parameterTypes));
        visitor.visitInsn(returnType.getOpcode(Opcodes.IRETURN));
        visitor.visitMaxs(parameterTypes.length, parameterTypes.length);
    }
    
    @Override
    public FieldVisitor visitField(int access, String name, String desc,
            String signature, Object value) {
        
        // Remove all non-static field definitions
        if ((Opcodes.ACC_STATIC & access) != 0) {
            return super.visitField(access, name, desc, signature, value);
        } else {
            return null;
        }
    }
    
    @Override
    public void visitEnd() {
        // Generate the constructor that takes a mirror instance
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
        
        super.visitEnd();
    }

    public static byte[] generate(String className, ClassReader reader) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES & ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = classWriter;
        visitor = new CheckClassAdapter(visitor);
        visitor = new MirageClassGenerator(visitor);
        visitor = new RemappingClassAdapter(visitor, REMAPPER);
        reader.accept(visitor, ClassReader.SKIP_FRAMES);
        return classWriter.toByteArray();
    }
}
