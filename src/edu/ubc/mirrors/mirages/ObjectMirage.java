package edu.ubc.mirrors.mirages;

import java.lang.reflect.InvocationTargetException;

import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.fieldmap.FieldMapMirror;
import edu.ubc.mirrors.raw.NativeCharArrayMirror;
import edu.ubc.mirrors.raw.NativeClassGenerator;
import edu.ubc.mirrors.raw.NativeClassMirrorLoader;
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
     * Redefined here so the code rewriting can correct getClass() - otherwise we hit the
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
        
        try {
            InstanceMirror mirror = (InstanceMirror)mirage.mirror;
            CharArrayMirror valueMirror = (CharArrayMirror)mirror.getMemberField("value").get();
            char[] value = new char[valueMirror.length()];
            NativeCharArrayMirror nativeValueMirror = new NativeCharArrayMirror(value);
            SystemStubs.arraycopy(valueMirror, 0, nativeValueMirror, 0, value.length);
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
    
    public static Object lift(Object object, Class<?> classLoaderLiteral) {
        if (object == null) {
            return null;
        }
        ObjectMirror mirror = new NativeObjectMirror(object);
        return ((MirageClassLoader)classLoaderLiteral.getClassLoader()).makeMirage(mirror);
    }
    
    public static InstanceMirror newInstanceMirror(Class<?> classLoaderLiteral, String className) throws ClassNotFoundException {
        MirageClassLoader loader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        // TODO-RS: Call ClassMirror.newInstanceMirror() instead!
        Class<?> originalClass = ObjectMirage.getOriginalClass(loader.loadClass(className.replace('/', '.')));
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
}
