package edu.ubc.retrospect;

import edu.ubc.mirrors.BlankInstanceMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.VirtualMachineMirror;

public class AroundClosureMirror extends BlankInstanceMirror {

    private final VirtualMachineMirror vm;
    private final MirrorInvocationHandler handler;
    
    public AroundClosureMirror(VirtualMachineMirror vm, MirrorInvocationHandler handler) {
        this.vm = vm;
        this.handler = handler;
    }

    public MirrorInvocationHandler getHandler() {
        return handler;
    }
    
    @Override
    public ClassMirror getClassMirror() {
        return vm.findBootstrapClassMirror(Object.class.getName());
    }
}
