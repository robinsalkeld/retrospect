package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.FieldMirrorGetHandlerRequest;

public class WrappingFieldMirrorGetHandlerRequest extends WrappingMirrorEventRequest implements FieldMirrorGetHandlerRequest {

    public WrappingFieldMirrorGetHandlerRequest(WrappingVirtualMachine vm, FieldMirrorGetHandlerRequest wrapped) {
        super(vm, wrapped);
    }
}
