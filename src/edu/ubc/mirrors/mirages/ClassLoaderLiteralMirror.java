package edu.ubc.mirrors.mirages;

import static edu.ubc.mirrors.mirages.MethodHandle.OBJECT_MIRAGE_MAKE_CLASS_MIRAGE;
import static edu.ubc.mirrors.mirages.MethodHandle.OBJECT_MIRAGE_MAKE_STRING_MIRAGE;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.fieldMirrorType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.mirageType;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class ClassLoaderLiteralMirror implements ClassMirror {

    public static final String CLASS_LOADER_LITERAL_NAME = "edu/ubc/mirrors/ClassLoaderLiteral";
    
    private final VirtualMachineMirror vm;
    private final ClassMirrorLoader loader;
    
    public ClassLoaderLiteralMirror(VirtualMachineMirror vm, ClassMirrorLoader loader) {
        this.vm = vm;
        this.loader = loader;
    }
    
    @Override
    public VirtualMachineMirror getVM() {
        return vm;
    }
    
    @Override
    public String getClassName() {
        return CLASS_LOADER_LITERAL_NAME;
    }

    @Override
    public ClassMirrorLoader getLoader() {
        return loader;
    }

    public static void getClassLoaderLiteralClass(MethodVisitor mv) {
        mv.visitTypeInsn(Opcodes.NEW, CLASS_LOADER_LITERAL_NAME);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, CLASS_LOADER_LITERAL_NAME, "<init>", "()V");
        
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                "java/lang/Object", 
                "getClass", 
                Type.getMethodDescriptor(Type.getType(Class.class)));
    }
    
    @Override
    public byte[] getBytecode() {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
//      ClassVisitor writer = new FrameAnalyzerAdaptor(getMirageClassMirrorLoader(), baseWriter);
        writer.visit(Opcodes.V1_1, Opcodes.ACC_PUBLIC, CLASS_LOADER_LITERAL_NAME, null, "java/lang/Object", null);
      
        // <init>
        String desc = Type.getMethodDescriptor(Type.VOID_TYPE);
        MethodVisitor mv = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", desc, null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", desc);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
        
        // makeMirage

        desc = Type.getMethodDescriptor(mirageType, Type.getType(ObjectMirror.class));
        mv = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "makeMirage", desc, null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);

        MethodHandle.OBJECT_MIRAGE_MAKE.invoke(mv);

        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(3, 1);
        mv.visitEnd();

        // makeStringMirage

        desc = Type.getMethodDescriptor(Type.getType(Mirage.class), Type.getType(String.class));
        mv = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "makeStringMirage", desc, null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.NEW, CLASS_LOADER_LITERAL_NAME);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, CLASS_LOADER_LITERAL_NAME, "<init>", "()V");

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                "java/lang/Object", 
                "getClass", 
                Type.getMethodDescriptor(Type.getType(Class.class)));
        
        OBJECT_MIRAGE_MAKE_STRING_MIRAGE.invoke(mv);
        
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(3, 1);
        mv.visitEnd();

        // makeClassMirage

        desc = Type.getMethodDescriptor(Type.getType(Mirage.class), Type.getType(Class.class));
        mv = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "makeClassMirage", desc, null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.NEW, CLASS_LOADER_LITERAL_NAME);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, CLASS_LOADER_LITERAL_NAME, "<init>", "()V");

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                "java/lang/Object", 
                "getClass", 
                Type.getMethodDescriptor(Type.getType(Class.class)));
        
        OBJECT_MIRAGE_MAKE_CLASS_MIRAGE.invoke(mv);
        
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(3, 1);
        mv.visitEnd();

        // getStaticField

        desc = Type.getMethodDescriptor(Type.getType(FieldMirror.class), Type.getType(String.class), Type.getType(String.class));
        mv = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "getStaticField", desc, null, null);
        mv.visitCode();
        mv.visitTypeInsn(Opcodes.NEW, CLASS_LOADER_LITERAL_NAME);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, CLASS_LOADER_LITERAL_NAME, "<init>", "()V");

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                "java/lang/Object", 
                "getClass", 
                Type.getMethodDescriptor(Type.getType(Class.class)));
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
                Type.getInternalName(ObjectMirage.class), 
                "getStaticField", 
                Type.getMethodDescriptor(fieldMirrorType, Type.getType(Class.class), Type.getType(String.class), Type.getType(String.class)));
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(3, 2);
        mv.visitEnd();

        // newInstanceMirror

        desc = Type.getMethodDescriptor(Type.getType(InstanceMirror.class), Type.getType(String.class));
        mv = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "newInstanceMirror", desc, null, null);
        mv.visitCode();
        mv.visitTypeInsn(Opcodes.NEW, CLASS_LOADER_LITERAL_NAME);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, CLASS_LOADER_LITERAL_NAME, "<init>", "()V");

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                "java/lang/Object", 
                "getClass", 
                Type.getMethodDescriptor(Type.getType(Class.class)));
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
                Type.getInternalName(ObjectMirage.class), 
                "newInstanceMirror", 
                Type.getMethodDescriptor(Type.getType(InstanceMirror.class), Type.getType(Class.class), Type.getType(String.class)));
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(3, 1);
        mv.visitEnd();
      
        // newArrayMirror(String, int)

        desc = Type.getMethodDescriptor(Type.getType(ArrayMirror.class), Type.getType(String.class), Type.INT_TYPE);
        mv = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "newArrayMirror", desc, null, null);
        mv.visitCode();
        mv.visitTypeInsn(Opcodes.NEW, CLASS_LOADER_LITERAL_NAME);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, CLASS_LOADER_LITERAL_NAME, "<init>", "()V");

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                "java/lang/Object", 
                "getClass", 
                Type.getMethodDescriptor(Type.getType(Class.class)));
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ILOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
                Type.getInternalName(ObjectMirage.class), 
                "newArrayMirror", 
                Type.getMethodDescriptor(Type.getType(ArrayMirror.class), Type.getType(Class.class), Type.getType(String.class), Type.INT_TYPE));
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(4, 1);
        mv.visitEnd();
        
        // newArrayMirror(String, int[])

        desc = Type.getMethodDescriptor(Type.getType(ArrayMirror.class), Type.getType(String.class), Type.getObjectType("[I"));
        mv = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "newArrayMirror", desc, null, null);
        mv.visitCode();
        mv.visitTypeInsn(Opcodes.NEW, CLASS_LOADER_LITERAL_NAME);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, CLASS_LOADER_LITERAL_NAME, "<init>", "()V");

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                "java/lang/Object", 
                "getClass", 
                Type.getMethodDescriptor(Type.getType(Class.class)));
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
                Type.getInternalName(ObjectMirage.class), 
                "newArrayMirror", 
                Type.getMethodDescriptor(Type.getType(ArrayMirror.class), Type.getType(Class.class), Type.getType(String.class), Type.getObjectType("[I")));
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(4, 1);
        mv.visitEnd();
        
        return writer.toByteArray();
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public ClassMirror getComponentClassMirror() {
        return null;
    }

    @Override
    public ClassMirror getSuperClassMirror() {
        return Reflection.loadClassMirrorInternal(this, Object.class.getName());
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public List<ClassMirror> getInterfaceMirrors() {
        return Collections.emptyList();
    }

    @Override
    public FieldMirror getStaticField(String name) throws NoSuchFieldException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public List<FieldMirror> getMemberFields() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public MethodMirror getMethod(String name, ClassMirror... paramTypes)
            throws SecurityException, NoSuchMethodException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ConstructorMirror getConstructor(ClassMirror... paramTypes)
            throws SecurityException, NoSuchMethodException {
        
        throw new NoSuchMethodException();
    }
    
    @Override
    public List<ConstructorMirror> getDeclaredConstructors(boolean publicOnly) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isPrimitive() {
        return false;
    }
    
    @Override
    public Map<String, ClassMirror> getDeclaredFields() {
        return Collections.emptyMap();
    }
    
    @Override
    public int getModifiers() {
        return Modifier.PUBLIC;
    }
    
    @Override
    public List<InstanceMirror> getInstances() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InstanceMirror newRawInstance() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ClassMirrorLoader newRawClassLoaderInstance() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ArrayMirror newArray(int size) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ArrayMirror newArray(int... dims) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ClassMirror getClassMirror() {
        return getVM().findBootstrapClassMirror(Class.class.getName());
    }
    
    @Override
    public boolean initialized() {
        return true;
    }
}
