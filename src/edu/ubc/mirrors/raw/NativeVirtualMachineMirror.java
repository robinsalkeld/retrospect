package edu.ubc.mirrors.raw;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class NativeVirtualMachineMirror implements VirtualMachineMirror {

    public static NativeVirtualMachineMirror INSTANCE = new NativeVirtualMachineMirror();
    
    public ClassMirror findBootstrapClassMirror(String name) {
        ClassLoader appClassLoader = ClassLoader.getSystemClassLoader();
        Method nativeMethod;
        try {
            nativeMethod = ClassLoader.class.getDeclaredMethod("findBootstrapClass", String.class);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
        nativeMethod.setAccessible(true);
        try {
            return (ClassMirror)NativeObjectMirror.makeMirror((Class<?>)nativeMethod.invoke(appClassLoader, name));
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    @Override
    public List<ClassMirror> findAllClasses(String name) {
        throw new UnsupportedOperationException();
    }
}
