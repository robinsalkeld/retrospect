package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.FieldMirrorSetRequest;

public class WrappingFieldMirrorSetRequest extends WrappingMirrorEventRequest implements FieldMirrorSetRequest {

    private final FieldMirrorSetRequest wrapped;
    
    public WrappingFieldMirrorSetRequest(WrappingVirtualMachine vm, FieldMirrorSetRequest wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }


}
