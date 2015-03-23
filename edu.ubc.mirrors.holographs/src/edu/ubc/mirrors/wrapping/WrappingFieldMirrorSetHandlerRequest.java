package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.FieldMirrorSetHandlerRequest;

public class WrappingFieldMirrorSetHandlerRequest extends WrappingMirrorEventRequest implements FieldMirrorSetHandlerRequest {

    public WrappingFieldMirrorSetHandlerRequest(WrappingVirtualMachine vm, FieldMirrorSetHandlerRequest wrapped) {
        super(vm, wrapped);
    }
}
