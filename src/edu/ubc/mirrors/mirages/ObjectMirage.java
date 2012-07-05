package edu.ubc.mirrors.mirages;

import static edu.ubc.mirrors.mirages.MirageClassGenerator.getOriginalBinaryClassName;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.raw.NativeClassGenerator;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.raw.nativestubs.java.lang.SystemStubs;

/**
 * Superclass for all mirage classes.
 * Represents java.lang.Object if used directly.
 * 
 * @author Robin Salkeld
 */
public class ObjectMirage implements Mirage {
    public ObjectMirror mirror;
    
    /**
     * Constructor for calls to make() - the mirror instance is passed up the constructor chain.
     */
    public ObjectMirage(Object mirror) {
        this.mirror = (ObjectMirror)mirror;
        register();
    }
    
    /**
     * Constructor for translated new statements.
     */
    public ObjectMirage(InstanceMirror mirror) {
        this.mirror = mirror;
        register();
    }
    
    private void register() {
        MirageClassLoader loader = ClassHolograph.getMirageClassLoader((ClassHolograph)mirror.getClassMirror());
        loader.registerMirage(this);
    }
    
    @Override
    public ObjectMirror getMirror() {
        return mirror;
    }
    
    public ClassMirror getClassMirror() {
        return mirror.getClassMirror();
    }
    
    /**
     * Redefined here so we can correct getClass() - otherwise we hit the
     * definition in Object.class which we can't rewrite.
     * @param o
     * @return
     */
    public static String mirageToString(Mirage o) {
        return o.getMirror().getClassMirror().getClassName() + "@" + Integer.toHexString(o.hashCode());
    }
    
    @Override
    public String toString() {
        return mirageToString(this);
    }
    
    public static Class<?> getNativeClass(Class<?> mirageClass) {
        final String originalClassName = MirageClassGenerator.getOriginalBinaryClassName(mirageClass.getName());
        final String nativeClassName = NativeClassGenerator.getNativeBinaryClassName(originalClassName);
        try {
            return mirageClass.getClassLoader().loadClass(nativeClassName);
        } catch (ClassNotFoundException e) {
            throw new InternalError();
        }
    }
    
    private static void throwInternalError(Throwable t) {
        InternalError ie = new InternalError();
        ie.initCause(t);
        throw ie;
    }
    
    public static String getRealStringForMirage(ObjectMirage mirage) {
        if (mirage == null) {
            return null;
        }
        
        return Reflection.getRealStringForMirror((InstanceMirror)mirage.mirror);
    }
    
    public static StackTraceElement[] getRealStackTraceForMirage(Mirage mirage) {
        if (mirage == null) {
            return null;
        }
        
        try {
            ObjectArrayMirror arrayMirror = (ObjectArrayMirror)mirage.getMirror();
            StackTraceElement[] result = new StackTraceElement[arrayMirror.length()];
            for (int i = 0; i < result.length; i++) {
                InstanceMirror element = (InstanceMirror)arrayMirror.get(i);
                String declaringClass = Reflection.getRealStringForMirror((InstanceMirror)element.getMemberField("declaringClass").get());
                String methodName = Reflection.getRealStringForMirror((InstanceMirror)element.getMemberField("methodName").get());
                String fileName = Reflection.getRealStringForMirror((InstanceMirror)element.getMemberField("fileName").get());
                int lineNumber = element.getMemberField("lineNumber").getInt();
                
                result[i] = new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
            }
            return result;
        } catch (IllegalAccessException e) {
            throwInternalError(e);
            return null;
        } catch (NoSuchFieldException e) {
            throwInternalError(e);
            return null;
        }
    }
    
    public static InstanceMirror newInstanceMirror(Class<?> classLoaderLiteral, String mirageClassName) {
        String originalClassName = MirageClassGenerator.getOriginalInternalClassName(mirageClassName);
        MirageClassLoader loader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        ClassMirror originalClassMirror = loader.loadOriginalClassMirror(Type.getObjectType(originalClassName));
        return originalClassMirror.newRawInstance();
    }
    
    public static ArrayMirror newArrayMirror(Class<?> classLoaderLiteral, String elementDescriptor, int length) {
        MirageClassLoader loader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        ClassMirror originalClassMirror = loader.loadOriginalClassMirror(Type.getType(elementDescriptor));
        return originalClassMirror.newArray(length);
    }
    
    public static ArrayMirror newArrayMirror(Class<?> classLoaderLiteral, String elementDescriptor, int[] dims) {
        MirageClassLoader loader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        ClassMirror originalClassMirror = loader.loadOriginalClassMirror(Type.getType(elementDescriptor));
        return originalClassMirror.newArray(dims);
    }
    
    public static FieldMirror getStaticField(Class<?> classLoaderLiteral, String className, String fieldName) throws NoSuchFieldException, ClassNotFoundException {
        if (fieldName.equals("INTERPRETED_FRAMES")) {
            int bp = 4;
            bp++;
        }
        
        MirageClassLoader loader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        String binaryName = className.replace('/', '.');
        ClassHolograph klass = loader.loadOriginalClassMirror(binaryName);
        
        // Force initialization just as the VM would, in case there is
        // a <clini> method that needs to be run.
        MirageClassLoader.initializeClassMirror(klass);
        
        try {
            return klass.getStaticField(fieldName);
        } catch (NoSuchFieldException e) {
            // Continue
        }
        
        // TODO-RS: Check the spec on the ordering here
        ClassMirror superclass = klass.getSuperClassMirror();
        if (superclass != null) {
            try {
                return superclass.getStaticField(fieldName);
            } catch (NoSuchFieldException e) {
                // Ignore
            }
        }
        
        for (ClassMirror i : klass.getInterfaceMirrors()) {
            try {
                return i.getStaticField(fieldName);
            } catch (NoSuchFieldException e) {
                // Ignore
            }
        }
        
        throw new NoSuchFieldException(fieldName);
    }
    
    public static Mirage make(ObjectMirror mirror) {
        if (mirror == null) {
            return null;
        }
        
        MirageClassLoader loader = ClassHolograph.getMirageClassLoader((ClassHolograph)mirror.getClassMirror());
        return loader.makeMirage(mirror);
    }
    
    public static Mirage makeStringMirage(String s, Class<?> classLoaderLiteral) {
        if (s == null) {
            return null;
        }
        
        MirageClassLoader loader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        InstanceMirror sMirror = Reflection.makeString(loader.getVM(), s);
        return loader.makeMirage(sMirror);
    }
    
    public static Mirage makeClassMirage(Class<?> c, Class<?> classLoaderLiteral) {
        if (c == null) {
            return null;
        }
        
        MirageClassLoader loader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        String originalClassName = MirageClassGenerator.getOriginalBinaryClassName(c.getName());
        ClassMirror originalMirror = loader.loadOriginalClassMirror(originalClassName);
        return loader.makeMirage(originalMirror);
    }
    
    public static ObjectMirror getMirror(Object o) {
        if (o == null) {
            return null;
        }
        return ((Mirage)o).getMirror();
    }
    
    public static Mirage cleanAndSetStackTrace(Mirage throwable, StackTraceElement[] nativeTrace) {
        VirtualMachineMirror vm = throwable.getMirror().getClassMirror().getVM();
        ClassMirror stackTraceElementClass = vm.findBootstrapClassMirror(StackTraceElement.class.getName());
        ClassMirror stringClass = vm.findBootstrapClassMirror(String.class.getName());
        ClassMirror intClass = vm.getPrimitiveClass("int");
        ConstructorMirror constructor = Reflection.getConstructor(stackTraceElementClass, stringClass, stringClass, stringClass, intClass);
                
        ObjectArrayMirror correctedTrace = (ObjectArrayMirror)stackTraceElementClass.newArray(nativeTrace.length);
        for (int i = 0; i < nativeTrace.length; i++) {
            StackTraceElement e = nativeTrace[i];
            InstanceMirror className = Reflection.makeString(vm, getOriginalBinaryClassName(e.getClassName()));
            InstanceMirror methodName = Reflection.makeString(vm, e.getMethodName());
            InstanceMirror fieldName = Reflection.makeString(vm, e.getFileName());
            int lineNumber = e.getLineNumber();
            InstanceMirror mapped = Reflection.newInstance(constructor, className, methodName, fieldName, lineNumber);
            correctedTrace.set(i, mapped);
        }
        InstanceMirror mirror = (InstanceMirror)throwable.getMirror();
        Reflection.setField(mirror, "stackTrace", correctedTrace);
        return make(correctedTrace);
    }
    
    // This has to be public since the implicit version on array classes is public
    @Override
    public Object clone() throws CloneNotSupportedException {
        return clone(this);
    }
    
    public static Object clone(Mirage mirage) {
        Class<?> classLoaderLiteral = mirage.getClass();
        ObjectMirror mirror = mirage.getMirror();
        if (mirror instanceof InstanceMirror) {
            InstanceMirror instanceMirror = (InstanceMirror)mirror;
            InstanceMirror result = newInstanceMirror(classLoaderLiteral, mirror.getClassMirror().getClassName());
            for (FieldMirror field : instanceMirror.getMemberFields()) {
                ClassMirror fieldType = field.getType();
                try {
                    FieldMirror resultField = result.getMemberField(field.getName());
                    if (fieldType.equals(Boolean.TYPE)) {
                        resultField.setBoolean(field.getBoolean());
                    } else if (fieldType.equals(Byte.TYPE)) {
                        resultField.setByte(field.getByte());
                    } else if (fieldType.equals(Character.TYPE)) {
                        resultField.setChar(field.getChar());
                    } else if (fieldType.equals(Short.TYPE)) {
                        resultField.setShort(field.getShort());
                    } else if (fieldType.equals(Integer.TYPE)) {
                        resultField.setInt(field.getInt());
                    } else if (fieldType.equals(Long.TYPE)) {
                        resultField.setLong(field.getLong());
                    } else if (fieldType.equals(Float.TYPE)) {
                        resultField.setFloat(field.getFloat());
                    } else if (fieldType.equals(Double.TYPE)) {
                        resultField.setDouble(field.getDouble());
                    } else {
                        resultField.set(field.get());
                    }
                } catch (NoSuchFieldException e) {
                    throw new NoSuchFieldError(e.getMessage());
                } catch (IllegalAccessException e) {
                    throw new IllegalAccessError(e.getMessage());
                }
            }
            return make(result);
        } else if (mirror instanceof ArrayMirror) {
            ArrayMirror objectArrayMirror = (ArrayMirror)mirror;
            int length = objectArrayMirror.length();
            String elementClassName = mirror.getClassMirror().getComponentClassMirror().getClassName();
            ArrayMirror result = newArrayMirror(classLoaderLiteral, Type.getObjectType(elementClassName).getDescriptor(), length);
            
            SystemStubs.arraycopyMirrors(objectArrayMirror, 0, result, 0, length);
            
            return make(result);
        } else {    
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }
}
