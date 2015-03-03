package edu.ubc.mirrors.wrapping;

import java.util.ArrayList;
import java.util.List;

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
    public Object invoke(List<Object> args, MirrorInvocationHandler original) throws MirrorInvocationTargetException {
        List<Object> unwrappedArgs = new ArrayList<Object>(args.size());
        for (Object arg : args) {
            unwrappedArgs.add(vm.unwrappedValue(arg));
        }
        MirrorInvocationHandler unwrappedOriginal = vm.unwrapInvocationHandler(original);
        Object result = wrapped.invoke(unwrappedArgs, unwrappedOriginal);
        return vm.wrapValue(result);
    }

}
