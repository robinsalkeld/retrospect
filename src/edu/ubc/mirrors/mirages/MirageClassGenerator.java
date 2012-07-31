package edu.ubc.mirrors.mirages;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.commons.Remapper;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.fieldmap.FieldMapMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.raw.NativeInstanceMirror;
import edu.ubc.mirrors.test.Breakpoint;

public class MirageClassGenerator extends ClassVisitor {

    public static Type objectMirrorType = Type.getType(ObjectMirror.class);
    public static Type instanceMirrorType = Type.getType(InstanceMirror.class);
    public static Type arrayMirrorType = Type.getType(ArrayMirror.class);
    public static Type objectArrayMirrorType = Type.getType(ObjectArrayMirror.class);
    public static Type objectMirageType = Type.getType(ObjectMirage.class);
    public static Type mirageType = Type.getType(Mirage.class);
    public static Type fieldMirrorType = Type.getType(FieldMirror.class);
    public static Type nativeObjectMirrorType = Type.getType(NativeInstanceMirror.class);
    public static Type fieldMapMirrorType = Type.getType(FieldMapMirror.class);
    public static Type objectType = Type.getType(Object.class);
    public static Type stringType = Type.getType(String.class);
    public static Type classType = Type.getType(Class.class);
    public static Type stackTraceElementType = Type.getType(StackTraceElement.class);
    public static Type stackTraceType = Type.getType(StackTraceElement[].class);
    
    public static Remapper REMAPPER = new Remapper() {
        public String map(String typeName) {
            return getMirageInternalClassName(typeName, false);
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
    
    private final boolean needsInitialization;
    
    private boolean isInterface;
    private String name;
    private String superName;
    
    private final Map<org.objectweb.asm.commons.Method, Method> mirrorMethods;
    
    public MirageClassGenerator(ClassHolograph classMirror, ClassVisitor output) {
        super(Opcodes.ASM4, output);
        this.needsInitialization = !classMirror.initialized();
        Class<?> nativeStubsClass = classMirror.getNativeStubsClass();
        mirrorMethods = indexStubMethods(nativeStubsClass);
    }
    
    public static Map<org.objectweb.asm.commons.Method, Method> indexStubMethods(Class<?> nativeStubsClass) {
        if (nativeStubsClass != null) {
            Map<org.objectweb.asm.commons.Method, Method> result = new HashMap<org.objectweb.asm.commons.Method, Method>();
            for (Method m : nativeStubsClass.getDeclaredMethods()) {
                org.objectweb.asm.commons.Method method = org.objectweb.asm.commons.Method.getMethod(m);
                result.put(method, m);
            }
            return result;
        } else {
            return Collections.emptyMap();
        }
    }
    
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.name = name;
        this.isInterface = (Opcodes.ACC_INTERFACE & access) != 0;
        this.superName = getMirageSuperclassName(isInterface, name, superName);
        interfaces = getMirageInterfaces(isInterface, interfaces);
        
        // Force everything to be public for now, since MirageClassLoader has to reflectively
        // construct mirages.
        // Also remove enum flags.
        int mirageAccess = (~(Opcodes.ACC_ENUM | Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED) & access) | Opcodes.ACC_PUBLIC;
        
        super.visit(version, mirageAccess, name, signature, this.superName, interfaces);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        int mirageAccess = ~Opcodes.ACC_ENUM & access;
        super.visitInnerClass(name, outerName, innerName, mirageAccess);
    }
    
    public static String getMirageSuperclassName(boolean isInterface, String mirageName, String mirageSuperName) {
        if (getMirageInternalClassName(Type.getInternalName(Throwable.class), true).equals(mirageName)) {
            return Type.getInternalName(Throwable.class);
        } else if (Type.getInternalName(Mirage.class).equals(mirageSuperName)) {
            if (isInterface) {
                return Type.getInternalName(Object.class);
            } else {
                return Type.getInternalName(ObjectMirage.class);
            }
        } else {
            return mirageSuperName;
        }
    }
    
    public static String[] getMirageInterfaces(boolean isInterface, String[] mirageInterfaces) {
        if (isInterface) {
            String[] newInterfaces = new String[mirageInterfaces.length + 1];
            System.arraycopy(mirageInterfaces, 0, newInterfaces, 0, mirageInterfaces.length);
            newInterfaces[newInterfaces.length - 1] = Type.getInternalName(Mirage.class);
            return newInterfaces;
        } else {
            return mirageInterfaces;
        }
    }
    
    public static String getMirageBinaryClassName(String className, boolean arrayImpl) {
        if (className == null) {
            return null;
        }
        
        return getMirageInternalClassName(className.replace('.', '/'), arrayImpl).replace('/', '.');
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
    
    public static boolean isRefType(Type t) {
        return t.getSort() == Type.OBJECT || t.getSort() == Type.ARRAY;
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
            return null;
        }
    }
    
    public static String getPrimitiveArrayMirageInternalName(Type elementType) {
        return "edu/ubc/mirrors/mirages/" + getSortName(elementType.getSort()) + "ArrayMirage";
    }
    
    public static String getPrimitiveArrayMirrorInternalName(Type elementType) {
        return "edu/ubc/mirrors/" + getSortName(elementType.getSort()) + "ArrayMirror";
    }
    
    public static String getMirageInternalClassName(String className, boolean impl) {
        if (className == null) {
            return null;
        }
        
        if (className.equals(Type.getInternalName(Object.class))) {
            return impl ? Type.getInternalName(ObjectMirage.class) : Type.getInternalName(Mirage.class);
        }
        if (className.equals("[L" + Type.getInternalName(Object.class) + ";")
                || className.equals("[L" + Type.getInternalName(Mirage.class) + ";")) {
            return impl ? Type.getInternalName(ObjectArrayMirage.class) : Type.getInternalName(ObjectArrayMirror.class);
        }
        
        Type type = Type.getObjectType(className);
        if (type.getSort() == Type.ARRAY) {
            Type elementType = type.getElementType();
            int elementSort = elementType.getSort();
            int dims = type.getDimensions();
            // Primitive array
            if (dims == 1 && elementSort != Type.OBJECT) {
                return getPrimitiveArrayMirageInternalName(elementType);
            } else {
                String elementName = (elementSort == Type.OBJECT ?
                        elementType.getInternalName() : getSortName(elementSort));
                return (impl ? "miragearrayimpl" : "miragearray") + dims + "/" + elementName; 
            }
        } else if (!className.startsWith("mirage")) {
            return "mirage/" + className;
        } else {
            return className;
        }
    }
    
    public static boolean isImplementationClass(String mirageClassBinaryName) {
        return mirageClassBinaryName.equals(ObjectMirage.class.getName())
            || mirageClassBinaryName.equals(ObjectArrayMirage.class.getName())
            || mirageClassBinaryName.startsWith("miragearrayimpl");
    }
    
    public static String getOriginalInternalClassName(String mirageClassName) {
        if (mirageClassName == null) {
            return null;
        }
        
        Matcher m = Pattern.compile("miragearray(?:impl)?(\\d+)[./](.*)").matcher(mirageClassName);
        if (m.matches()) {
            int dims = Integer.parseInt(m.group(1));
            String mirageElementName = m.group(2);
            Type originalElementType = getTypeForSortName(mirageElementName);
            if (originalElementType == null) {
                originalElementType = Type.getObjectType(getOriginalInternalClassName(mirageElementName));
            }
            
            return makeArrayType(dims, originalElementType).getInternalName();
        }
        
        if (Type.getInternalName(Mirage.class).equals(mirageClassName) || Type.getInternalName(ObjectMirage.class).equals(mirageClassName)) {
            return "java/lang/Object";
        }
        
        if (Type.getInternalName(ObjectArrayMirage.class).equals(mirageClassName)) {
            return "[Ljava/lang/Object;";
        }
        
        if (Type.getInternalName(ArrayMirage.class).equals(mirageClassName)) {
            return mirageClassName;
        }
        
        m = Pattern.compile("edu/ubc/mirrors/mirages/(.*)ArrayMirage").matcher(mirageClassName);
        if (m.matches()) {
            String sortName = m.group(1);
            return "[" + getTypeForSortName(sortName).getDescriptor();
        }
        
        if (mirageClassName.equals("edu/ubc/mirrors/ObjectArrayMirror")) {
            return "[Ljava/lang/Object;";
        }
        
        if (mirageClassName.startsWith("mirage")) {
            return mirageClassName.substring("mirage".length() + 1);
        } else {
            return mirageClassName;
        }
    }
    
    public static String getOriginalBinaryClassName(String mirageBinaryName) {
        return getOriginalInternalClassName(mirageBinaryName.replace('.', '/')).replace('/', '.');
    }
    
    public static Type makeArrayType(int dims, Type elementType) {
        if (dims == 0) {
            return elementType;
        }
        StringBuilder builder = new StringBuilder();
        while (dims-- > 0) {
            builder.append('[');
        }
        builder.append(elementType.getDescriptor());
        return Type.getObjectType(builder.toString());
    }
    
    public static Type getMirageType(Type type) {
        if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY) {
            return Type.getObjectType(getMirageInternalClassName(type.getInternalName(), false));
        } else {
            return type;
        }
        
    }
    
    public static Type getOriginalType(Type type) {
        if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY) {
            return Type.getObjectType(getOriginalInternalClassName(type.getInternalName()));
        } else {
            return type;
        }
        
    }
    
    public static Type getMirageType(Class<?> c) {
        return getMirageType(Type.getType(c));
    }
    
    public static String addMirrorParam(String desc) {
        Type type = Type.getMethodType(desc);
        Type argTypes[] = type.getArgumentTypes();
        Type newArgTypes[] = new Type[argTypes.length + 1];
        System.arraycopy(argTypes, 0, newArgTypes, 0, argTypes.length);
        newArgTypes[newArgTypes.length - 1] = instanceMirrorType;
        return Type.getMethodDescriptor(type.getReturnType(), newArgTypes);
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {

        // Remove all static initializers for classes that were already defined - 
        // these will have already been executed when
        // loading the original class and state will be proxied by ClassMirrors instead.
        if (!needsInitialization && name.equals("<clinit>")) {
            return null;
        }
        
        if (name.equals("<init>")) {
            // Add the implicit mirror argument
            desc = addMirrorParam(desc);
        }
        
        Type mirageThrowableType = getMirageType(Throwable.class);
        
        // toString() is a special case - it's defined in java.lang.Object, which this class must ultimately
        // extend, so we have to return a real String rather than a mirage.
        boolean isToString = name.equals("toString") && desc.equals(Type.getMethodDescriptor(getMirageType(Type.getType(String.class))));
        if (isToString) {
            desc = Type.getMethodDescriptor(Type.getType(String.class));
        }
        if (name.equals("equals") && desc.equals(Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(Mirage.class)))) {
            desc = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(Object.class));
        }
        boolean isGetStackTrace = this.name.equals(mirageThrowableType.getInternalName()) && name.equals("getStackTrace") && desc.equals(Type.getMethodDescriptor(getMirageType(Type.getType(StackTraceElement[].class)))); 
        if (isGetStackTrace) {
            desc = Type.getMethodDescriptor(Type.getType(StackTraceElement[].class));
        }
        boolean isGetOurStackTrace = name.equals("getOurStackTrace") && desc.equals(Type.getMethodDescriptor(getMirageType(Type.getType(StackTraceElement[].class)))); 
        if (name.equals("getStackTraceElement") && desc.equals(Type.getMethodDescriptor(getMirageType(Type.getType(StackTraceElement.class)), Type.INT_TYPE))) {
            desc = Type.getMethodDescriptor(Type.getType(StackTraceElement.class), Type.INT_TYPE);
        }
        
        // Take off the native keyword if it's there - we're going to fill in an actual
        // method (even if it's a stub that throws an exception).
        int mirageAccess = ~Opcodes.ACC_NATIVE & access;
        MethodVisitor superVisitor = super.visitMethod(mirageAccess, name, desc, signature, exceptions);
        
        if (this.name.equals(mirageThrowableType.getInternalName())) {
            if (name.equals("fillInStackTrace") && desc.equals(Type.getMethodDescriptor(mirageThrowableType))) {
                superVisitor.visitCode();
                superVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                superVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, superName, name, Type.getMethodDescriptor(Type.getType(Throwable.class)));
                superVisitor.visitInsn(Opcodes.POP);
                superVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                superVisitor.visitInsn(Opcodes.ARETURN);
                superVisitor.visitMaxs(1, 1);
                superVisitor.visitEnd();
                
                return null;
            } else if (isGetOurStackTrace) {
                superVisitor.visitCode();
                superVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                superVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                superVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "getStackTrace", 
                                                                    Type.getMethodDescriptor(stackTraceType));
                superVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, objectMirageType.getInternalName(), 
                                                                    "cleanAndSetStackTrace", 
                                                                    Type.getMethodDescriptor(stackTraceType, mirageType, stackTraceType));
                superVisitor.visitTypeInsn(Opcodes.CHECKCAST, getMirageType(stackTraceType).getInternalName());
                superVisitor.visitInsn(Opcodes.ARETURN);
                superVisitor.visitMaxs(2, 1);
                superVisitor.visitEnd();
                
                return null;
            }
        }
        
        MirageMethodGenerator generator = new MirageMethodGenerator(this.name, mirageAccess, name, desc, superVisitor, isToString, isGetStackTrace);
        LocalVariablesSorter lvs = new LocalVariablesSorter(access, desc, generator);
        generator.setLocalVariablesSorter(lvs);
        
        Type methodType = Type.getMethodType(desc);
        Type[] argumentTypes = methodType.getArgumentTypes();
        List<Type> stubArgumentTypes = new ArrayList<Type>(argumentTypes.length + 1);
        stubArgumentTypes.add(Type.getType(Class.class));
        if ((Opcodes.ACC_STATIC & access) == 0) {
            stubArgumentTypes.add(getStubType(Type.getObjectType(this.name)));
        }
        for (int i = 0; i < argumentTypes.length; i++) {
            stubArgumentTypes.add(getStubType(argumentTypes[i]));
        }
        Type stubReturnType = getStubType(methodType.getReturnType());
        org.objectweb.asm.commons.Method methodDesc = new org.objectweb.asm.commons.Method(name, stubReturnType, 
                stubArgumentTypes.toArray(new Type[stubArgumentTypes.size()]));
        Method mirrorMethod = mirrorMethods.get(methodDesc);
        if (mirrorMethod != null) {
            generateStaticThunk(superVisitor, desc, mirrorMethod);
            
            return null;
        } else if ((Opcodes.ACC_NATIVE & access) != 0) {
            // Generate a method body that throws an exception
            generator.visitCode();
            Type exceptionType = Type.getType(InternalError.class); 
            generator.anew(exceptionType);
            generator.dup();
            String message = "Unsupported native method: " + this.name + "#" + name + desc;
            generator.aconst(message);
            generator.invokespecial(exceptionType.getInternalName(), 
                                    "<init>", 
                                    Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class)));
            generator.athrow();
            generator.visitMaxs(1, 0);
            generator.visitEnd();

            return null;
        }
        
        return lvs;
    }
    
    public static Type getStubMethodType(String owner, int access, Type methodType) {
        Type[] argumentTypes = methodType.getArgumentTypes();
        List<Type> stubArgumentTypes = new ArrayList<Type>(argumentTypes.length + 1);
        stubArgumentTypes.add(Type.getType(Class.class));
        if ((Opcodes.ACC_STATIC & access) == 0) {
            stubArgumentTypes.add(getStubType(Type.getObjectType(owner)));
        }
        for (int i = 0; i < argumentTypes.length; i++) {
            stubArgumentTypes.add(getStubType(argumentTypes[i]));
        }
        Type stubReturnType = getStubType(methodType.getReturnType());
        return Type.getMethodType(stubReturnType, stubArgumentTypes.toArray(new Type[stubArgumentTypes.size()]));
    }
    
    private static Type getStubType(Type type) {
        switch (type.getSort()) {
        case Type.OBJECT:
        case Type.ARRAY:
            return mirageType;
        default:
            return type;
        }
    }

    public static void generateStaticThunk(MethodVisitor visitor, String desc, Method method) {
        Class<?>[] parameterClasses = method.getParameterTypes();
        visitor.visitCode();
        
        assert parameterClasses[0].equals(Class.class);
        ClassLoaderLiteralMirror.getClassLoaderLiteralClass(visitor);
        int var = 0;
        for (int param = 1; param < parameterClasses.length; param++) {
            Type paramType = Type.getType(parameterClasses[param]);
            visitor.visitVarInsn(paramType.getOpcode(Opcodes.ILOAD), var);
            var += paramType.getSize();
        }
        
        Type returnType = Type.getType(method.getReturnType());
        
        visitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(method.getDeclaringClass()), method.getName(), Type.getMethodDescriptor(method));
        
        if (isRefType(returnType)) {
            visitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getReturnType(desc).getInternalName());
        }
        visitor.visitInsn(returnType.getOpcode(Opcodes.IRETURN));
        visitor.visitMaxs(parameterClasses.length + 1, parameterClasses.length + 1);
        visitor.visitEnd();
    }
    
    @Override
    public FieldVisitor visitField(int access, String name, String desc,
            String signature, Object value) {
        
        // Remove all field definitions
        return null;
    }
    
    @Override
    public void visitEnd() {
        // Generate the dummy static field used to force initialization
        if (!isInterface) {
            super.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "initialized", Type.BOOLEAN_TYPE.getDescriptor(), null, false);
        }
        
        // Generate the constructor that takes a mirror instance as an Object parameter
        String constructorDesc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class));
        if (name.equals(getMirageInternalClassName(Type.getInternalName(Throwable.class), true))) {
            // This doesn't extend ObjectMirage so we have to set the field directly
            super.visitField(Opcodes.ACC_PUBLIC, "mirror", objectMirrorType.getDescriptor(), null, null);
            
            MethodVisitor methodVisitor = super.visitMethod(Opcodes.ACC_PUBLIC, 
                             "<init>", constructorDesc, null, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE));
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, objectMirrorType.getInternalName());
            methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, name, "mirror", Type.getDescriptor(ObjectMirror.class));
            methodVisitor.visitInsn(Opcodes.RETURN);
            methodVisitor.visitMaxs(2, 2);
            methodVisitor.visitEnd();
            
            methodVisitor = super.visitMethod(Opcodes.ACC_PUBLIC, 
                    "getMirror", Type.getMethodDescriptor(objectMirrorType), null, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitFieldInsn(Opcodes.GETFIELD, name, "mirror", Type.getDescriptor(ObjectMirror.class));
            methodVisitor.visitInsn(Opcodes.ARETURN);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        } else if (!isInterface) {
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

    public static byte[] generateArray(MirageClassLoader loader, ClassMirror classMirror, boolean isInterface) {
        String internalName = getMirageInternalClassName(classMirror.getClassName().replace('.', '/'), !isInterface);
        
        Type originalType = Type.getObjectType(classMirror.getClassName().replace('.', '/'));
        Type originalElementType = originalType.getElementType();
        int dims = originalType.getDimensions();
        
        ClassMirror superClassMirror = null;
        String superName = isInterface ? Type.getInternalName(Object.class) : Type.getInternalName(ObjectArrayMirage.class);
        Set<String> interfaces = new HashSet<String>();
        int access = Opcodes.ACC_PUBLIC | (isInterface ? Opcodes.ACC_INTERFACE : 0);
        
        if (originalElementType.getSort() == Type.OBJECT || originalElementType.getSort() == Type.ARRAY) {
            ClassMirror elementClass = loader.loadOriginalClassMirror(originalElementType.getClassName());
            superClassMirror = elementClass.getSuperClassMirror();
            
            if (isInterface) {
                if (superClassMirror != null) { 
                    Type superType = makeArrayType(dims, Type.getObjectType(superClassMirror.getClassName().replace('.', '/'))); 
                    String superInterfaceName = getMirageType(superType).getInternalName(); 
                    interfaces.add(superInterfaceName);
                }
                
                for (ClassMirror interfaceMirror : elementClass.getInterfaceMirrors()) {
                    Type superType = makeArrayType(dims, Type.getObjectType(interfaceMirror.getClassName().replace('.', '/'))); 
                    String interfaceName = getMirageType(superType).getInternalName(); 
                    interfaces.add(interfaceName);
                }
                
                interfaces.add(mirageType.getInternalName());
                
                Type nMinus1Type = makeArrayType(dims - 1, Type.getType(Object.class)); 
                interfaces.add(getMirageType(nMinus1Type).getInternalName());
            } else {
                interfaces.add(getMirageInternalClassName(classMirror.getClassName().replace('.', '/'), false));
            }
        }
        
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V1_1, access, internalName, null, superName, interfaces.toArray(new String[0]));

        if (isInterface) {
            // Generate clone()
            String cloneDesc = Type.getMethodDescriptor(objectType);
            MethodVisitor mv = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, "clone", cloneDesc, null, null);
            mv.visitEnd();
        } else {
            // Generate thunk constructors
            String initDesc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(ObjectArrayMirror.class));
            MethodVisitor mv = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", initDesc, null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>", initDesc);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
            
            initDesc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE);
            mv = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", initDesc, null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>", initDesc);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        return writer.toByteArray();
    }
}
