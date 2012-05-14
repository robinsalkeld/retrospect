package edu.ubc.mirrors.raw;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.InstanceMirror;

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
    public InstanceMirror newInstance(Object... args)
            throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        
        Object[] nativeArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            nativeArgs[i] = NativeMethodMirror.getNativeObject(args[i]);
        }
        Object result = nativeConstructor.newInstance(nativeArgs);
        return (InstanceMirror)NativeInstanceMirror.makeMirror(result);
    }
    
    @Override
    public int getSlot() {
        try {
            InstanceMirror nativeMirror = (InstanceMirror)NativeInstanceMirror.makeMirror(nativeConstructor);
            return nativeMirror.getMemberField("slot").getInt();
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
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
    public byte[] getAnnotations() {
        return (byte[])readField("annotations");
    }
    
    @Override
    public byte[] getParameterAnnotations() {
        return (byte[])readField("parameterAnnotations");
    }
}
