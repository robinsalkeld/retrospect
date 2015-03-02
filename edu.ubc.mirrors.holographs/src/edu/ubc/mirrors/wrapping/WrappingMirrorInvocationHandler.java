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
    public Object invoke(Object[] args, MirrorInvocationHandler original) throws MirrorInvocationTargetException {
        Object[] unwrappedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            unwrappedArgs[i] = vm.unwrappedValue(args[i]);
        }
        MirrorInvocationHandler unwrappedOriginal = vm.unwrapInvocationHandler(original);
        Object result = wrapped.invoke(unwrappedArgs, unwrappedOriginal);
        return vm.wrapValue(result);
    }

}
