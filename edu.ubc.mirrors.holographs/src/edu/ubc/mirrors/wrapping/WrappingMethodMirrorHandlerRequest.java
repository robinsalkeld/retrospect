package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorHandlerRequest;

public class WrappingMethodMirrorHandlerRequest extends WrappingMirrorEventRequest implements MethodMirrorHandlerRequest {

    private final MethodMirrorHandlerRequest wrapped;
    
    public WrappingMethodMirrorHandlerRequest(WrappingVirtualMachine vm, MethodMirrorHandlerRequest wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    @Override
    public void setMethodFilter(MethodMirror method) {
        wrapped.setMethodFilter(vm.unwrapMethodMirror(method));
    }
}
