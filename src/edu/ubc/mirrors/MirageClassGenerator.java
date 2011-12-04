package edu.ubc.mirrors;

import java.io.PrintWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

public class MirageClassGenerator extends ClassVisitor {

    public static Type objectMirrorType = Type.getType(ObjectMirror.class);
    public static Type objectMirageType = Type.getType(ObjectMirage.class);
    public static Type fieldMirrorType = Type.getType(FieldMirror.class);
    
    public MirageClassGenerator(ClassVisitor output) {
        super(Opcodes.ASM4, output);
    }
    
    private static Type liftMirages(Type t) {
        return t.getSort() == Type.OBJECT ? objectMirageType : t;
    }
    
    private String superName = null;
    
    @Override
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        this.superName = superName;
        if (superName.equals(Type.getInternalName(Object.class))) {
            this.superName = Type.getInternalName(ObjectMirage.class);
        }
        
        super.visit(version, access, name, signature, this.superName, interfaces);
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
        
        // Change all object reference types to ObjectMirage
        Type methodType = Type.getType(desc);
        
        Type liftedReturnType = liftMirages(methodType.getReturnType());
        Type[] argTypes = methodType.getArgumentTypes();
        Type[] liftedArgTypes = new Type[argTypes.length];
        for (int i = 0; i < argTypes.length; i++) {
            liftedArgTypes[i] = liftMirages(argTypes[i]);
        }
        String liftedDesc = Type.getMethodDescriptor(liftedReturnType, liftedArgTypes);

        // TODO: handle signatures as well (T => ObjectMirage<T>)
        
        MirageMethodGenerator generator = new MirageMethodGenerator(super.visitMethod(access, name, liftedDesc, signature, exceptions));
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
    
    public static byte[] generate(ClassReader reader) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES & ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = classWriter;
//        visitor = new TraceClassVisitor(visitor, new PrintWriter(System.out));
        visitor = new CheckClassAdapter(visitor);
        visitor = new MirageClassGenerator(visitor);
        reader.accept(visitor, ClassReader.SKIP_FRAMES);
        return classWriter.toByteArray();
    }
}
