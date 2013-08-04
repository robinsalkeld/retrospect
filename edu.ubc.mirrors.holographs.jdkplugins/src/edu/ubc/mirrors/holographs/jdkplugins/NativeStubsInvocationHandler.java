package edu.ubc.mirrors.holographs.jdkplugins;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.objectweb.asm.Opcodes;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.holographs.MirrorInvocationHandler;

public class NativeStubsInvocationHandler implements MirrorInvocationHandler {

    private final Object stubsClassInstance;
    private final Method stubsMethod;
    
    public NativeStubsInvocationHandler(Object stubsClassInstance, Method stubsMethod) {
        this.stubsClassInstance = stubsClassInstance;
        this.stubsMethod = stubsMethod;
    }

    @Override
    public Object invoke(InstanceMirror object, MethodMirror method, Object[] args) throws MirrorInvocationTargetException {
        Object[] stubsArgs = args;
        if ((Opcodes.ACC_STATIC & method.getModifiers()) == 0) {
            stubsArgs = new Object[args.length + 1];
            stubsArgs[0] = object;
            System.arraycopy(args, 0, stubsArgs, 1, args.length);
        }
        try {
            return stubsMethod.invoke(stubsClassInstance, stubsArgs);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof MirrorInvocationTargetException) {
                throw (MirrorInvocationTargetException)e.getCause();
            }
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
