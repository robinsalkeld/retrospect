package edu.ubc.mirrors;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;

public abstract class ObjectMirage<T> {

    protected final ObjectMirror<T> mirror;
    
    public ObjectMirage(ObjectMirror<T> mirror) {
        this.mirror = mirror;
        
    }
    
    public ObjectMirror<T> getMirror() {
        return mirror;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T make(ObjectMirror<T> mirror) {
        try {
            final Class<?> mirageClass = mirror.getClassMirror();
            final Constructor<?> c = mirageClass.getConstructor(ObjectMirror.class);
            return (T)c.newInstance(mirror);
        } catch (NoSuchMethodException e) {
            InternalError error = new InternalError("Mirage class constructor not accessible: " + mirror.getClassMirror());
            error.initCause(e);
            throw error;
        } catch (SecurityException e) {
            InternalError error = new InternalError("Mirage class constructor not accessible: " + mirror.getClassMirror());
            error.initCause(e);
            throw error;
        } catch (IllegalAccessException e) {
            InternalError error = new InternalError("Mirage class constructor not accessible: " + mirror.getClassMirror());
            error.initCause(e);
            throw error;
        } catch (InstantiationException e) {
            InternalError error = new InternalError("Result of ObjectMirage#getMirrorClass() is abstract: " + mirror.getClassMirror());
            error.initCause(e);
            throw error;
        } catch (InvocationTargetException e) {
            InternalError error = new InternalError("Error on instantiating Mirage class: " + mirror.getClassMirror());
            error.initCause(e);
            throw error;
        }
    }
    
    private static Map<ClassLoader, MirageClassLoader> mirageClassLoaders = new HashMap<ClassLoader, MirageClassLoader>();
    
    public static MirageClassLoader getMirageClassLoader(ClassLoader originalLoader) {
        MirageClassLoader mirageLoader = mirageClassLoaders.get(originalLoader);
        if (mirageLoader == null) {
            mirageLoader = new MirageClassLoader(originalLoader, 
                        "/Users/robinsalkeld/Documents/UBC/Code/Retrospect/mirage_classes/",
                        "/Users/robinsalkeld/Documents/UBC/Code/Retrospect/native_classes/");
            mirageClassLoaders.put(originalLoader, mirageLoader);
        }
        return mirageLoader;
    }
    
    public static Class<?> getMirageClass(Class<?> original) throws ClassNotFoundException {
        MirageClassLoader mirageLoader = getMirageClassLoader(original.getClassLoader());
        return mirageLoader.loadClass(MirageClassGenerator.getMirageBinaryClassName(original.getName()));
    }
    
    public static Class<?> getNativeClass(Class<?> mirageClass) {
        if (MirageClassLoader.COMMON_CLASSES.containsKey(mirageClass.getName())) {
            
        }
        
        
        final String originalClassName = MirageClassGenerator.getOriginalClassName(mirageClass.getName());
        final String nativeClassName = NativeClassGenerator.getNativeBinaryClassName(originalClassName);
        try {
            return mirageClass.getClassLoader().loadClass(nativeClassName);
        } catch (ClassNotFoundException e) {
            throw new InternalError();
        }
    }
    
    public static Class<?> defineMirageClass(String className, ClassLoader originalLoader, ClassReader originalReader) {
        MirageClassLoader mirageLoader = getMirageClassLoader(originalLoader);
        return mirageLoader.defineMirageClass(className, originalReader);
    }
    
    public static Class<?> getOriginalClass(Class<?> mirageClass) throws ClassNotFoundException {
        ClassLoader originalLoader = ((MirageClassLoader)mirageClass.getClassLoader()).getOriginalLoader();
        String originalClassName = MirageClassGenerator.getOriginalClassName(mirageClass.getName());
        return originalLoader.loadClass(originalClassName);
    }
    
    private static void throwInternalError(Throwable t) {
        InternalError ie = new InternalError();
        ie.initCause(t);
        throw ie;
    }
    
    public static void invokeMirageMainMethod(Class<?> originalClass, String[] args) {
        try {
            final Class<?> mirageClass = getMirageClass(originalClass);
            final Class<?> mirageStringArray = Class.forName("[Ljava.lang.String;", true, mirageClass.getClassLoader());
            mirageClass.getDeclaredMethod("main", mirageStringArray).invoke(null, (Object)args);
        } catch (ClassNotFoundException e) {
            throwInternalError(e);
        } catch (SecurityException e) {
            throwInternalError(e);
        } catch (IllegalAccessException e) {
            throwInternalError(e);
        } catch (InvocationTargetException e) {
            throwInternalError(e);
        } catch (NoSuchMethodException e) {
            throwInternalError(e);
        }
    }
    
    public static String getRealStringForMirage(ObjectMirage<?> mirage) {
        try {
            ObjectMirror<?> mirror = mirage.getMirror();
            char[] value = (char[])mirror.getMemberField("value").get();
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
}
