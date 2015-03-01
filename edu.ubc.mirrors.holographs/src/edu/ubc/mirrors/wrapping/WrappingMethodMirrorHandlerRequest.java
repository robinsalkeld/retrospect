package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.MethodMirrorHandlerRequest;

public class WrappingMethodMirrorHandlerRequest extends WrappingMirrorEventRequest implements MethodMirrorHandlerRequest {

    public WrappingMethodMirrorHandlerRequest(WrappingVirtualMachine vm, MethodMirrorHandlerRequest wrapped) {
        super(vm, wrapped);
    }

}
