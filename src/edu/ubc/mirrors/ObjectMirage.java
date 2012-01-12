package edu.ubc.mirrors;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;

public class ObjectMirage<T> {

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
            final Class<?> mirageClass = getMirageClass(mirror.getClassMirror());
            System.out.println("mirageClass loader: " + mirageClass.getClassLoader());
            final Constructor<?> c = mirageClass.getConstructor(ObjectMirror.class);
            return (T)c.newInstance(mirror);
        } catch (ClassNotFoundException e) {
            InternalError error = new InternalError("No mirage class registered for class: " + mirror.getClassMirror());
            error.initCause(e);
            throw error;
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
            mirageLoader = new MirageClassLoader(originalLoader, null);
            mirageClassLoaders.put(originalLoader, mirageLoader);
        }
        return mirageLoader;
    }
    
    public static Class<?> getMirageClass(Class<?> original) throws ClassNotFoundException {
        MirageClassLoader mirageLoader = getMirageClassLoader(original.getClassLoader());
        return mirageLoader.loadClass(original.getName());
    }
    
    public static Class<?> defineMirageClass(String className, ClassLoader originalLoader, ClassReader originalReader) {
        MirageClassLoader mirageLoader = getMirageClassLoader(originalLoader);
        return mirageLoader.defineMirageClass(className, originalReader);
    }
    
    public static int invokeMirageMainMethod(Class<?> originalClass, String[] args) {
        try {
            final Class<?> mirageClass = getMirageClass(originalClass);
            mirageClass.getMethods();
            return ((Integer)mirageClass.getDeclaredMethod("main", String[].class).invoke(null, (Object)args)).intValue();
        } catch (ClassNotFoundException e) {
            InternalError error = new InternalError("No mirage class registered for class: " + originalClass);
            error.initCause(e);
            throw error;
        } catch (SecurityException e) {
            InternalError ie = new InternalError();
            ie.initCause(e);
            throw ie;
        } catch (IllegalAccessException e) {
            InternalError ie = new InternalError();
            ie.initCause(e);
            throw ie;
        } catch (InvocationTargetException e) {
            InternalError ie = new InternalError();
            ie.initCause(e);
            throw ie;
        } catch (NoSuchMethodException e) {
            InternalError ie = new InternalError();
            ie.initCause(e);
            throw ie;
        }
    }
}
