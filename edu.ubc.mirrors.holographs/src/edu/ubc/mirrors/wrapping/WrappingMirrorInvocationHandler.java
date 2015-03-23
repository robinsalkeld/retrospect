package edu.ubc.mirrors.wrapping;

import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.InvocableMirror;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ThreadMirror;

public class WrappingMirrorInvocationHandler implements MirrorInvocationHandler {

    private final WrappingVirtualMachine vm;
    protected final MirrorInvocationHandler wrapped;
    
    public WrappingMirrorInvocationHandler(WrappingVirtualMachine vm, MirrorInvocationHandler wrapped) {
        this.vm = vm;
        this.wrapped = wrapped;
    }

    @Override
    public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
        ThreadMirror unwrappedThread = vm.unwrapThread(thread);
        List<Object> unwrappedArgs = new ArrayList<Object>(args.size());
        for (Object arg : args) {
            unwrappedArgs.add(vm.unwrappedValue(arg));
        }
        Object result = wrapped.invoke(unwrappedThread, unwrappedArgs);
        return vm.wrapValue(result);
    }

}
