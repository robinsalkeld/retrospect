package edu.ubc.mirrors;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

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
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;

public class MirageClassGenerator extends ClassVisitor {

    public static Type objectMirrorType = Type.getType(ObjectMirror.class);
    public static Type objectMirageType = Type.getType(ObjectMirage.class);
    public static Type fieldMirrorType = Type.getType(FieldMirror.class);
    public static Type nativeObjectMirrorType = Type.getType(NativeObjectMirror.class);
    public static Type fieldMapMirrorType = Type.getType(FieldMapMirror.class);
    
    public static Remapper REMAPPER = new Remapper() {
        public String map(String typeName) {
            return getMirageInternalClassName(typeName);
        };
    };
    
    public MirageClassGenerator(ClassVisitor output) {
        super(Opcodes.ASM4, output);
    }
    
    private String superName = null;
    private boolean isInterface;
    
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
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
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {

        // toString() is a special case - it's defined in java.lang.Object, which this class must ultimately
        // extend, so we have to return a real String rather than a mirage.
        boolean isToString = (name.equals("toString") && desc.equals(Type.getMethodType(Type.getType(String.class))));
        
        if (name.equals("<init>")) {
            desc = addMirrorArgToDesc(desc);
        }
        MethodVisitor superVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        MirageMethodGenerator generator = new MirageMethodGenerator(superVisitor, superName, Type.getMethodType(desc), isToString);
        LocalVariablesSorter lvs = new LocalVariablesSorter(access, desc, generator);
        if (name.equals("<init>")) {
            lvs.newLocal(objectMirrorType);
        }
        generator.setLocalVariablesSorter(lvs);
        return lvs;
    }
    
    public static String addMirrorArgToDesc(String desc) {
        Type methodType = Type.getMethodType(desc);
        Type[] argTypes = methodType.getArgumentTypes();
        Type[] argTypesWithMirrorParam = new Type[argTypes.length + 1];
        System.arraycopy(argTypes, 0, argTypesWithMirrorParam, 0, argTypes.length);
        argTypesWithMirrorParam[argTypes.length] = objectMirrorType;
        return Type.getMethodDescriptor(methodType.getReturnType(), argTypesWithMirrorParam);
    }
    
    @Override
    public FieldVisitor visitField(int access, String name, String desc,
            String signature, Object value) {
        
        // Remove all field definitions
        return null;
    }

    public static byte[] generate(String className, ClassReader reader, String traceDir) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES & ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = classWriter;
        visitor = new CheckClassAdapter(visitor);
        visitor = new MirageClassGenerator(visitor);
        visitor = new RemappingClassAdapter(visitor, REMAPPER);
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
}
