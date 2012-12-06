package edu.ubc.mirrors.raw;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;

public class NativeMethodMirror implements MethodMirror {

    private final Method nativeMethod;
    
    public NativeMethodMirror(Method nativeMethod) {
        this.nativeMethod = nativeMethod;
    }
    
    @Override
    public Object invoke(ThreadMirror thread, ObjectMirror obj, Object... args)
            throws IllegalArgumentException, IllegalAccessException,
            MirrorInvocationTargetException {

        if (!thread.equals(NativeInstanceMirror.makeMirror(Thread.currentThread()))) {
            throw new IllegalThreadStateException("The native VM can only invoke methods on the current thread");
        }
        
        Object[] nativeArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            nativeArgs[i] = getNativeObject(args[i]);
        }
        Object nativeObj = getNativeObject(obj);
        Object result;
        try {
            result = nativeMethod.invoke(nativeObj, nativeArgs);
        } catch (InvocationTargetException e) {
            throw new MirrorInvocationTargetException((InstanceMirror)NativeInstanceMirror.makeMirror(e.getCause()));
        }
        return wrapNativeObject(result);
    }

    static Object getNativeObject(Object obj) {
        if (obj instanceof NativeInstanceMirror) {
            return ((NativeInstanceMirror)obj).getNativeObject();
        } else {
            return obj;
        }
    }
    
    static Object wrapNativeObject(Object obj) {
        if (obj instanceof NativeInstanceMirror) {
            return ((NativeInstanceMirror)obj).getNativeObject();
        } else {
            return obj;
        }
    }
    
    @Override
    public void setAccessible(boolean flag) {
        nativeMethod.setAccessible(flag);
    }

    @Override
    public String getName() {
        return nativeMethod.getName();
    }

    @Override
    public List<String> getParameterTypeNames() {
        List<String> result = new ArrayList<String>();
        for (Class<?> klass : nativeMethod.getParameterTypes()) {
            result.add(klass.getName());
        }
        return result;
    }
    
    @Override
    public List<ClassMirror> getParameterTypes() {
        List<ClassMirror> result = new ArrayList<ClassMirror>();
        for (Class<?> klass : nativeMethod.getParameterTypes()) {
            result.add(new NativeClassMirror(klass));
        }
        return result;
    }

    @Override
    public String getReturnTypeName() {
        return nativeMethod.getReturnType().getName();
    }
    
    @Override
    public ClassMirror getReturnType() {
        return new NativeClassMirror(nativeMethod.getReturnType());
    }

    @Override
    public byte[] getRawAnnotations() {
        // TODO For now
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getRawParameterAnnotations() {
        // TODO For now
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getRawAnnotationDefault() {
        // TODO For now
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassMirror getDeclaringClass() {
        // TODO For now
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSlot() {
        // TODO For now
        throw new UnsupportedOperationException();
    }

    @Override
    public int getModifiers() {
        // TODO For now
        throw new UnsupportedOperationException();
    }
    
    @Override
    public List<String> getExceptionTypeNames() {
        // TODO For now
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ClassMirror> getExceptionTypes() {
        // TODO For now
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSignature() {
        // TODO For now
        throw new UnsupportedOperationException();
    }
    
}
