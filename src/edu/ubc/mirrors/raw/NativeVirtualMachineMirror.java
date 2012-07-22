package edu.ubc.mirrors.raw;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class NativeVirtualMachineMirror implements VirtualMachineMirror {

    public static NativeVirtualMachineMirror INSTANCE = new NativeVirtualMachineMirror();
    
    private static final Method nativeMethod;
    static {
        try {
            nativeMethod = ClassLoader.class.getDeclaredMethod("findBootstrapClass", String.class);
            nativeMethod.setAccessible(true);
        } catch (SecurityException e) {
            throw new InternalError();
        } catch (NoSuchMethodException e) {
            throw new InternalError();
        }
    }
    
    public ClassMirror findBootstrapClassMirror(String name) {
        try {
            // TODO-RS: Do we need to call the native findBootstrapClass() method directly?
            // Is this shortcut harmful?
            Class<?> klass = Class.forName(name, false, null);
            if (klass.getClassLoader() == null) {
                return new NativeClassMirror(klass);
            } else {
                return null;
            }
        } catch (ClassNotFoundException e) {
            return null;
        }
//        ClassLoader appClassLoader = ClassLoader.getSystemClassLoader();
//        try {
//            return (ClassMirror)NativeInstanceMirror.makeMirror((Class<?>)nativeMethod.invoke(appClassLoader, name));
//        } catch (IllegalAccessException e) {
//            throw new IllegalAccessError(e.getMessage());
//        } catch (InvocationTargetException e) {
//            throw new RuntimeException(e);
//        }
    }
    
    
    @Override
    public List<ClassMirror> findAllClasses(String name, boolean includeSubclasses) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public List<ThreadMirror> getThreads() {
        throw new UnsupportedOperationException();
    }
    
    public static Class<?> getNativePrimitiveClass(String name) {
        if (name.equals("boolean")) {
            return Boolean.TYPE;
        } else if (name.equals("byte")) {
            return Byte.TYPE;
        } else if (name.equals("char")) {
            return Character.TYPE;
        } else if (name.equals("short")) {
            return Short.TYPE;
        } else if (name.equals("int")) {
            return Integer.TYPE;
        } else if (name.equals("long")) {
            return Long.TYPE;
        } else if (name.equals("float")) {
            return Float.TYPE;
        } else if (name.equals("double")) {
            return Double.TYPE;
        } else {
            throw new IllegalArgumentException(name);
        }
    }
    
    @Override
    public ClassMirror getPrimitiveClass(String name) {
        return new NativeClassMirror(getNativePrimitiveClass(name));
    }
    
    @Override
    public ClassMirror getArrayClass(int dimensions, ClassMirror elementClass) {
        return new ArrayClassMirror(dimensions, elementClass);
    }
}
