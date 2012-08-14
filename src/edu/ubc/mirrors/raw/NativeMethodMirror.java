package edu.ubc.mirrors.raw;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
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
            InvocationTargetException {

        if (!thread.equals(NativeInstanceMirror.makeMirror(Thread.currentThread()))) {
            throw new IllegalThreadStateException("The native VM can only invoke methods on the current thread");
        }
        
        Object[] nativeArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            nativeArgs[i] = getNativeObject(args[i]);
        }
        Object nativeObj = getNativeObject(obj);
        Object result = nativeMethod.invoke(nativeObj, nativeArgs);
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
}
