package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorHandlerRequest;

public class WrappingConstructorMirrorHandlerRequest extends WrappingMirrorEventRequest implements ConstructorMirrorHandlerRequest {

    private final ConstructorMirrorHandlerRequest wrapped;
    
    public WrappingConstructorMirrorHandlerRequest(WrappingVirtualMachine vm, ConstructorMirrorHandlerRequest wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    @Override
    public void setConstructorFilter(ConstructorMirror constructor) {
        wrapped.setConstructorFilter(vm.unwrapConstructorMirror(constructor));
    }
}
