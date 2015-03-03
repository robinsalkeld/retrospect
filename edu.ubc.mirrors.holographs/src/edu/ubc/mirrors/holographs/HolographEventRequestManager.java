package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.MethodMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.wrapping.WrappingMirrorEventRequestManager;

public class HolographEventRequestManager extends WrappingMirrorEventRequestManager {

    private final VirtualMachineHolograph vm;
    
    public HolographEventRequestManager(VirtualMachineHolograph vm, MirrorEventRequestManager wrapped) {
        super(vm, wrapped);
        this.vm = vm;
    }
    
    @Override
    public MethodMirrorHandlerRequest createMethodMirrorHandlerRequest(MirrorInvocationHandler handler) {
        return new MethodHolographHandlerRequest(vm, handler);
    }
}

