package edu.ubc.mirrors.raw;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;

public class NativeConstructorMirror implements ConstructorMirror {

    private final Constructor<?> nativeConstructor;
    
    public NativeConstructorMirror(Constructor<?> nativeConstructor) {
        this.nativeConstructor = nativeConstructor;
    }
    

    @Override
    public InstanceMirror newInstance(ObjectMirror... args)
            throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        
        Object[] nativeArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            nativeArgs[i] = NativeMethodMirror.getNativeObject(args[i]);
        }
        Object result = nativeConstructor.newInstance(nativeArgs);
        return (InstanceMirror)NativeObjectMirror.makeMirror(result);
    }
}
