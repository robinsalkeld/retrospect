package edu.ubc.mirrors.raw;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.objectweb.asm.Type;

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
    
    @Override
    public ClassMirror getPrimitiveClass(String name) {
        if (name.equals("boolean")) {
            return new NativeClassMirror(Boolean.TYPE);
        } else if (name.equals("byte")) {
            return new NativeClassMirror(Byte.TYPE);
        } else if (name.equals("char")) {
            return new NativeClassMirror(Character.TYPE);
        } else if (name.equals("short")) {
            return new NativeClassMirror(Short.TYPE);
        } else if (name.equals("int")) {
            return new NativeClassMirror(Integer.TYPE);
        } else if (name.equals("long")) {
            return new NativeClassMirror(Long.TYPE);
        } else if (name.equals("float")) {
            return new NativeClassMirror(Float.TYPE);
        } else if (name.equals("double")) {
            return new NativeClassMirror(Double.TYPE);
        } else {
            throw new IllegalArgumentException(name);
        }
    }
    
    @Override
    public ClassMirror getArrayClass(int dimensions, ClassMirror elementClass) {
        return new ArrayClassMirror(dimensions, elementClass);
    }
}
