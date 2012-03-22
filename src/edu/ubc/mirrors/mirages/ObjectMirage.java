package edu.ubc.mirrors.mirages;

import static edu.ubc.mirrors.mirages.MirageClassGenerator.getOriginalBinaryClassName;

import java.util.Map;

import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.fieldmap.DirectArrayMirror;
import edu.ubc.mirrors.fieldmap.FieldMapMirror;
import edu.ubc.mirrors.raw.NativeCharArrayMirror;
import edu.ubc.mirrors.raw.NativeClassGenerator;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.raw.NativeObjectMirror;
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
    }
    
    /**
     * Constructor for translated new statements.
     */
    public ObjectMirage(InstanceMirror mirror) {
        this.mirror = mirror;
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
    
    // TODO: Can't do this until we sort out how to automatically specify ClassMirrorLoaders

//    private static Map<ClassLoader, MirageClassLoader> mirageClassLoaders = new HashMap<ClassLoader, MirageClassLoader>();
//    
//    public static MirageClassLoader getMirageClassLoader(ClassLoader originalLoader) {
//        MirageClassLoader mirageLoader = mirageClassLoaders.get(originalLoader);
//        if (mirageLoader == null) {
//            mirageLoader = new MirageClassLoader(originalLoader);
//            mirageClassLoaders.put(originalLoader, mirageLoader);
//        }
//        return mirageLoader;
//    }
//    
//    public static Class<?> getMirageClass(Class<?> original) throws ClassNotFoundException {
//        MirageClassLoader mirageLoader = getMirageClassLoader(original.getClassLoader());
//        return mirageLoader.loadClass(MirageClassGenerator.getMirageBinaryClassName(original.getName()));
//    }
    
    public static Class<?> getNativeClass(Class<?> mirageClass) {
        final String originalClassName = MirageClassGenerator.getOriginalBinaryClassName(mirageClass.getName());
        final String nativeClassName = NativeClassGenerator.getNativeBinaryClassName(originalClassName);
        try {
            return mirageClass.getClassLoader().loadClass(nativeClassName);
        } catch (ClassNotFoundException e) {
            throw new InternalError();
        }
    }
    
    // TODO: Can't do this until we sort out how to automatically specify ClassMirrorLoaders
//    public static Class<?> defineMirageClass(String className, ClassLoader originalLoader, ClassReader originalReader) {
//        MirageClassLoader mirageLoader = getMirageClassLoader(originalLoader);
//        return mirageLoader.defineMirageClass(className, originalReader);
//    }
    
    public static Class<?> getOriginalClass(Class<?> mirageClass) {
        if (mirageClass.equals(ObjectArrayMirage.class)) {
            return Object[].class;
        }
        
        ClassLoader originalLoader = ((MirageClassLoader)mirageClass.getClassLoader()).getOriginalLoader();
        String originalClassName = MirageClassGenerator.getOriginalBinaryClassName(mirageClass.getName());
        try {
            return Class.forName(originalClassName, true, originalLoader);
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
        
        return getRealStringForMirror((InstanceMirror)mirage.mirror);
    }
    
    public static String getRealStringForMirror(InstanceMirror mirror) {
        if (mirror == null) {
            return null;
        }
        
        try {
            CharArrayMirror valueMirror = (CharArrayMirror)mirror.getMemberField("value").get();
            char[] value = new char[valueMirror.length()];
            NativeCharArrayMirror nativeValueMirror = new NativeCharArrayMirror(value);
            SystemStubs.arraycopyMirrors(valueMirror, 0, nativeValueMirror, 0, value.length);
            int offset = mirror.getMemberField("offset").getInt();
            int count = mirror.getMemberField("count").getInt();
            return new String(value, offset, count);
        } catch (IllegalAccessException e) {
            throwInternalError(e);
            return null;
        } catch (NoSuchFieldException e) {
            throwInternalError(e);
        }
        
        // Never reached
        return null;
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
                String declaringClass = getRealStringForMirror((InstanceMirror)element.getMemberField("declaringClass").get());
                String methodName = getRealStringForMirror((InstanceMirror)element.getMemberField("methodName").get());
                String fileName = getRealStringForMirror((InstanceMirror)element.getMemberField("fileName").get());
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
    
    public static Object lift(Object object, Class<?> classLoaderLiteral) {
        return ((MirageClassLoader)classLoaderLiteral.getClassLoader()).lift(object);
    }
    
    public static InstanceMirror newInstanceMirror(Class<?> classLoaderLiteral, String className) {
        MirageClassLoader loader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        // TODO-RS: Call ClassMirror.newInstanceMirror() instead!
        Class<?> originalClass;
        try {
            originalClass = ObjectMirage.getOriginalClass(loader.loadClass(className.replace('/', '.')));
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getMessage());
        }
        return new FieldMapMirror(originalClass);
    }
    
    public static FieldMirror getStaticField(Class<?> classLoaderLiteral, String className, String fieldName) throws NoSuchFieldException, ClassNotFoundException {
        MirageClassLoader loader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        String binaryName = className.replace('/', '.');
        return loader.classMirrorLoader.loadClassMirror(binaryName).getStaticField(fieldName);
    }
    
    public static Object make(ObjectMirror mirror) {
        return ((MirageClassLoader)mirror.getClass().getClassLoader()).makeMirage(mirror);
    }
    
    public static Object make(ObjectMirror mirror, Class<?> classLoaderLiteral) {
        MirageClassLoader loader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        return loader.makeMirage(mirror);
    }
    
    public static ObjectMirror getMirror(Object o) {
        if (o == null) {
            return null;
        }
        return ((Mirage)o).getMirror();
    }
    
    public static StackTraceElement cleanStackTraceElement(StackTraceElement e) {
        return new StackTraceElement(getOriginalBinaryClassName(e.getClassName()), e.getMethodName(), e.getFileName(), e.getLineNumber());
    }
    
    public static StackTraceElement[] cleanAndSetStackTrace(Mirage throwable, StackTraceElement[] nativeTrace) {
        StackTraceElement[] correctedTrace = new StackTraceElement[nativeTrace.length];
        for (int i = 0; i < nativeTrace.length; i++) {
            correctedTrace[i] = ObjectMirage.cleanStackTraceElement(nativeTrace[i]);
        }
        InstanceMirror mirror = (InstanceMirror)throwable.getMirror();
        try {
            mirror.getMemberField("stackTrace").set(NativeObjectMirror.makeMirror(correctedTrace));
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
        return correctedTrace;
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return clone(this);
    }
    
    public static Object clone(Mirage mirage) {
        Class<?> classLoaderLiteral = mirage.getClass();
        ObjectMirror mirror = mirage.getMirror();
        if (mirror instanceof InstanceMirror) {
            InstanceMirror instanceMirror = (InstanceMirror)mirror;
            InstanceMirror result = newInstanceMirror(classLoaderLiteral, mirror.getClassMirror().getClassName());
            for (FieldMirror field : instanceMirror.getMemberFields()) {
                Class<?> fieldType = field.getType();
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
            return make(result, classLoaderLiteral);
        } else if (mirror instanceof ObjectArrayMirror) {
            ObjectArrayMirror objectArrayMirror = (ObjectArrayMirror)mirror;
            int length = objectArrayMirror.length();
            Class<?> originalClass = ObjectMirage.getOriginalClass(mirage.getClass());
            // TODO-RS: Call some method on ClassMirror or ClassLoaderMirror instead, ala newInstance
            ObjectArrayMirror result = new DirectArrayMirror(new NativeClassMirror(originalClass), length);
            
            SystemStubs.arraycopyMirrors(objectArrayMirror, 0, result, 0, length);
            
            return make(result, classLoaderLiteral);
        } else {    
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }
}
