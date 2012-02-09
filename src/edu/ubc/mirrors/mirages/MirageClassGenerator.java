package edu.ubc.mirrors.mirages;

import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.util.CheckClassAdapter;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.fieldmap.FieldMapMirror;
import edu.ubc.mirrors.raw.NativeObjectMirror;

public class MirageClassGenerator extends ClassVisitor {

    public static Type objectMirrorType = Type.getType(ObjectMirror.class);
    public static Type instanceMirrorType = Type.getType(InstanceMirror.class);
    public static Type arrayMirrorType = Type.getType(ArrayMirror.class);
    public static Type objectArrayMirrorType = Type.getType(ObjectArrayMirror.class);
    public static Type objectMirageType = Type.getType(ObjectMirage.class);
    public static Type fieldMirrorType = Type.getType(FieldMirror.class);
    public static Type nativeObjectMirrorType = Type.getType(NativeObjectMirror.class);
    public static Type fieldMapMirrorType = Type.getType(FieldMapMirror.class);
    public static Type classType = Type.getType(Class.class);
    
    public static Remapper REMAPPER = new Remapper() {
        public String map(String typeName) {
            return getMirageInternalClassName(typeName);
        }
        public String mapDesc(String desc) {
            Type t = Type.getType(desc);
            return mapTypeCorrected(t).getDescriptor();
        }
        private Type mapTypeCorrected(Type t) {
            switch (t.getSort()) {
                case Type.ARRAY:
                case Type.OBJECT:
                    String s = map(t.getInternalName());
                    return s != null ? Type.getObjectType(s) : t;
                case Type.METHOD:
                    return Type.getMethodType(mapMethodDesc(t.getDescriptor()));
            }
            return t;
        }
        public String mapType(String type) {
            if (type == null) {
                return null;
            }
            return mapTypeCorrected(Type.getObjectType(type)).getInternalName();
        };
        public Object mapValue(Object value) {
            if (value instanceof Type) {
                return mapTypeCorrected((Type)value);
            }
            return super.mapValue(value);
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
    
    public static String getSortName(int sort) {
        switch (sort) {
        case Type.BOOLEAN: 
            return "Boolean";
        case Type.BYTE:
            return "Byte";
        case Type.CHAR:
            return "Char";
        case Type.SHORT:
            return "Short";
        case Type.INT:
            return "Int";
        case Type.LONG:
            return "Long";
        case Type.FLOAT:
            return "Float";
        case Type.DOUBLE:
            return "Double";
        case Type.ARRAY:
            return "Array";
        case Type.OBJECT: 
            return "Object";
        default:
            throw new IllegalStateException("Bad sort: " + sort);
        }
    }
    
    public static Type getTypeForSortName(String name) {
        if (name.equals("Boolean")) {
            return Type.BOOLEAN_TYPE;
        } else if (name.equals("Byte")) {
            return Type.BYTE_TYPE;
        } else if (name.equals("Char")) {
            return Type.CHAR_TYPE;
        } else if (name.equals("Short")) {
            return Type.SHORT_TYPE;
        } else if (name.equals("Int")) {
            return Type.INT_TYPE;
        } else if (name.equals("Long")) {
            return Type.LONG_TYPE;
        } else if (name.equals("Float")) {
            return Type.FLOAT_TYPE;
        } else if (name.equals("Double")) {
            return Type.DOUBLE_TYPE;
        } else {
            throw new IllegalArgumentException("Bad sort name: " + name);
        }
    }
    
    public static String getPrimitiveArrayMirrorInternalName(Type elementType) {
        return "edu/ubc/mirrors/" + getSortName(elementType.getSort()) + "ArrayMirror";
    }
    
    public static String getMirageInternalClassName(String className) {
        if (className == null) {
            return null;
        }
        
        if (MirageClassLoader.COMMON_CLASSES.containsKey(className.replace('/', '.'))) {
            return className;
        }
        
//        System.out.println("className: " + className);
        Type type = Type.getObjectType(className);
//        System.out.println("type.getSort(): " + type.getSort());
        if (type.getSort() == Type.ARRAY) {
            Type elementType = type.getElementType();
            int elementSort = elementType.getSort();
            int dims = type.getDimensions();
            // Primitive array
            if (dims == 1 && elementSort != Type.OBJECT) {
                return getPrimitiveArrayMirrorInternalName(elementType);
            } else {
                String elementName = (elementSort == Type.OBJECT ?
                        elementType.getInternalName() : getSortName(elementSort));
                return "miragearray" + dims + "/" + elementName; 
            }
        } else if (!className.startsWith("mirage")) {
            return "mirage/" + className;
        } else {
            return className;
        }
    }
    
    public static String getOriginalClassName(String mirageClassName) {
        Matcher m = Pattern.compile("miragearray(\\d+)[./](.*)").matcher(mirageClassName);
        if (m.matches()) {
            int dims = Integer.parseInt(m.group(1));
            String result = "L" + m.group(2) + ";";
            while (dims-- > 0) {
                result = "[" + result;
            }
            return result;
        }
        
        m = Pattern.compile("edu[/.]ubc[/.]mirrors[/.](.*)ArrayMirror").matcher(mirageClassName);
        if (m.matches()) {
            String sortName = m.group(1);
            return "[" + getTypeForSortName(sortName).getDescriptor();
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

        // Remove all static initializers - these will have already been executed when
        // loading the original class and state will be proxied by ClassMirrors instead.
        // TODO: ...actually we can't do that because there seems to be no way to access private
        // classes at all. :( Instead for now we'll recreate static state in the mirage classes
        // and not yet use ClassMirror#getStaticField()
//        if (name.equals("<clinit>")) {
//            return null;
//        }
        
        // toString() is a special case - it's defined in java.lang.Object, which this class must ultimately
        // extend, so we have to return a real String rather than a mirage.
        boolean isToString = (name.equals("toString") && desc.equals(Type.getMethodDescriptor(getMirageType(Type.getType(String.class)))));
        if (isToString) {
            desc = Type.getMethodDescriptor(Type.getType(String.class));
        }
        
        // Take off the native keyword if it's there - we're going to fill in an actual
        // method (even if it's a stub that throws an exception).
        int noNativeAccess = ~Opcodes.ACC_NATIVE & access;
        MethodVisitor superVisitor = super.visitMethod(noNativeAccess, name, desc, signature, exceptions);
        
        AnalyzerAdapter analyzer = new AnalyzerAdapter(this.name, noNativeAccess, name, desc, superVisitor);
        MirageMethodGenerator generator = new MirageMethodGenerator(analyzer, superName, Type.getMethodType(desc), isToString);
        LocalVariablesSorter lvs = new LocalVariablesSorter(access, desc, generator);
        generator.setLocalVariablesSorter(lvs);
        
        if ((Opcodes.ACC_NATIVE & access) != 0) {
            String systemName = Type.getInternalName(System.class);
            String mirageSystemName = getMirageInternalClassName(systemName);
            if (name.equals("retrieveDirectives")) {
                int bp = 4;
                bp++;
            }
            if (this.name.equals(getMirageInternalClassName(Type.getInternalName(Class.class))) && name.equals("registerNatives")) {
                generator.visitCode();
                generator.visitInsn(Opcodes.RETURN);
                generator.visitMaxs(0, 0);
                generator.visitEnd();
                
                return null;
            } else if (this.name.equals(mirageSystemName) && name.equals("registerNatives")) {
                generator.visitCode();
                generator.visitInsn(Opcodes.RETURN);
                generator.visitMaxs(0, 0);
                generator.visitEnd();
                
                return null;
            } else if (this.name.equals(mirageSystemName) && name.equals("setIn0")) {
                generator.visitCode();
                generator.visitVarInsn(Opcodes.ALOAD, 0);
                generator.visitFieldInsn(Opcodes.PUTSTATIC, mirageSystemName, "in", Type.getDescriptor(InputStream.class));
                generator.visitMaxs(1, 1);
                generator.visitEnd();
                
                return null;
            } else if (this.name.equals(mirageSystemName) && name.equals("setOut0")) {
                generator.visitCode();
                generator.visitVarInsn(Opcodes.ALOAD, 0);
                generator.visitFieldInsn(Opcodes.PUTSTATIC, mirageSystemName, "out", Type.getDescriptor(PrintStream.class));
                generator.visitMaxs(1, 1);
                generator.visitEnd();
                
                return null;
            } else if (this.name.equals(mirageSystemName) && name.equals("setErr0")) {
                generator.visitCode();
                generator.visitVarInsn(Opcodes.ALOAD, 0);
                generator.visitFieldInsn(Opcodes.PUTSTATIC, mirageSystemName, "err", Type.getDescriptor(PrintStream.class));
                generator.visitMaxs(1, 1);
                generator.visitEnd();
                
                return null;
            } else if (this.name.equals(mirageSystemName) && (name.equals("currentTimeMillis") || name.equals("nanoTime"))) {
                generator.visitCode();
                generator.visitMethodInsn(Opcodes.INVOKESTATIC, systemName, name, Type.getMethodDescriptor(Type.LONG_TYPE));
                generator.visitMaxs(1, 1);
                generator.visitEnd();
                
                return null;
            } else if (this.name.equals(mirageSystemName) && (name.equals("identityHashCode"))) {
                // TODO-RS: Not correct for mirages!
                generator.visitCode();
                generator.load(0, Type.getType(Object.class));
                generator.visitMethodInsn(Opcodes.INVOKESTATIC, systemName, name, Type.getMethodDescriptor(Type.INT_TYPE));
                generator.visitMaxs(1, 1);
                generator.visitEnd();
                
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
                generator.visitEnd();
                
                return null;
            } else if (this.name.equals(mirageSystemName) && (name.equals("initProperties"))) {
                generator.visitCode();
                generator.putstatic(mirageSystemName, "props", getMirageType(Properties.class).getDescriptor());
                generator.areturn(Type.VOID_TYPE);
                generator.visitMaxs(1, 1);
                generator.visitEnd();
                
                return null;
            } else if (this.name.equals("mirage/java/lang/ClassLoader") && (name.equals("retrieveDirectives"))) {
                generator.visitCode();
                generator.aconst(null);
                generator.areturn(Type.getObjectType("mirage/java/lang/AssertionStatusDirectives"));
                generator.visitMaxs(1, 1);
                generator.visitEnd();
                
                return null;
            } else if (this.name.equals(mirageSystemName) && (name.equals("mapLibraryName"))) {
                generateStaticThunk(generator, System.class, name, String.class);
                
                return null;
            } else if (this.name.equals(classType.getInternalName()) && (name.equals("getPrimitiveClass"))) {
                generator.visitCode();
                generator.aconst(null);
                generator.areturn(classType);
                generator.visitEnd();
                
                return null;
            } else {
                // Generate a method body that throws an exception
                generator.visitCode();
                Type exceptionType = Type.getType(UnsupportedOperationException.class); 
                generator.anew(exceptionType);
                generator.dup();
                String message = "Unsupported native method: " + this.name + "#" + name;
                generator.aconst(message);
                generator.invokespecial(exceptionType.getInternalName(), 
                                        "<init>", 
                                        Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class)));
                generator.athrow();
                generator.visitMaxs(1, 0);
                generator.visitEnd();

                return null;
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
        if (!isInterface) {
            String constructorDesc = Type.getMethodDescriptor(Type.VOID_TYPE, instanceMirrorType);
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
