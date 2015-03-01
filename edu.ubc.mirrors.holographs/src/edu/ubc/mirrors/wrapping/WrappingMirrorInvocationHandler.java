package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;

public class WrappingMirrorInvocationHandler implements MirrorInvocationHandler {

    private final WrappingVirtualMachine vm;
    protected final MirrorInvocationHandler wrapped;
    
    public WrappingMirrorInvocationHandler(WrappingVirtualMachine vm, MirrorInvocationHandler wrapped) {
        this.vm = vm;
        this.wrapped = wrapped;
    }

    @Override
    public Object invoke(InstanceMirror target, MethodMirror method, Object[] args) throws MirrorInvocationTargetException {
        InstanceMirror unwrappedTarget = vm.unwrapInstanceMirror(target);
        MethodMirror unwrappedMethod = vm.unwrapMethodMirror(method);
        Object[] unwrappedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            unwrappedArgs[i] = vm.unwrappedValue(args[i]);
        }
        Object result = wrapped.invoke(unwrappedTarget, unwrappedMethod, unwrappedArgs);
        return vm.wrapValue(result);
    }

}
