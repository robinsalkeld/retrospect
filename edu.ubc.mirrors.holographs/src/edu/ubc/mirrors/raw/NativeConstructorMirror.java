package edu.ubc.mirrors.raw;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;

public class NativeConstructorMirror extends NativeInstanceMirror implements ConstructorMirror {

    private final Constructor<?> nativeConstructor;
    
    public NativeConstructorMirror(Constructor<?> nativeConstructor) {
        super(nativeConstructor);
        this.nativeConstructor = nativeConstructor;
    }
    
    @Override
    public ClassMirror getDeclaringClass() {
        return (ClassMirror)NativeInstanceMirror.makeMirror(nativeConstructor.getDeclaringClass());
    }
    
    @Override
    public InstanceMirror newInstance(ThreadMirror thread, Object... args)
            throws IllegalAccessException, IllegalArgumentException, MirrorInvocationTargetException {
        
        if (!thread.equals(NativeInstanceMirror.makeMirror(Thread.currentThread()))) {
            throw new IllegalThreadStateException("The native VM can only invoke methods on the current thread");
        }
        
        Object[] nativeArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            nativeArgs[i] = NativeMethodMirror.getNativeObject(args[i]);
        }
        Object result;
        try {
            result = nativeConstructor.newInstance(nativeArgs);
        } catch (InvocationTargetException e) {
            throw ClassHolograph.causeAsMirrorInvocationTargetException(e);
        } catch (InstantiationException e) {
            throw ClassHolograph.causeAsMirrorInvocationTargetException(e);
        }
        return (InstanceMirror)NativeInstanceMirror.makeMirror(result);
    }
    
    @Override
    public int getSlot() {
        try {
            return Constructor.class.getDeclaredField("slot").getInt(nativeConstructor);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
    }
    
    @Override
    public List<String> getParameterTypeNames() {
        List<String> result = new ArrayList<String>();
        Class<?>[] parameterTypes = nativeConstructor.getParameterTypes();
        for (Class<?> klass : parameterTypes) {
            result.add(klass.getName());
        }
        return result;
    }
    
    @Override
    public List<ClassMirror> getParameterTypes() {
        List<ClassMirror> result = new ArrayList<ClassMirror>();
        Class<?>[] parameterTypes = nativeConstructor.getParameterTypes();
        for (Class<?> klass : parameterTypes) {
            result.add((ClassMirror)NativeInstanceMirror.make(klass));
        }
        return result;
    }
    
    @Override
    public List<String> getExceptionTypeNames() {
        List<String> result = new ArrayList<String>();
        Class<?>[] parameterTypes = nativeConstructor.getExceptionTypes();
        for (Class<?> klass : parameterTypes) {
            result.add(klass.getName());
        }
        return result;
    }
    
    @Override
    public List<ClassMirror> getExceptionTypes() {
        List<ClassMirror> result = new ArrayList<ClassMirror>();
        Class<?>[] parameterTypes = nativeConstructor.getExceptionTypes();
        for (Class<?> klass : parameterTypes) {
            result.add((ClassMirror)NativeInstanceMirror.make(klass));
        }
        return result;
    }
    
    @Override
    public int getModifiers() {
        return nativeConstructor.getModifiers();
    }
    
    private Object readField(String name) {
        try {
            Field f = Constructor.class.getDeclaredField(name);
            f.setAccessible(true);
            return f.get(nativeConstructor);
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
        
    }
    
    @Override
    public String getSignature() {
        return (String)readField("signature");
    }
    
    @Override
    public byte[] getRawAnnotations() {
        return (byte[])readField("annotations");
    }
    
    @Override
    public byte[] getRawParameterAnnotations() {
        return (byte[])readField("parameterAnnotations");
    }
}