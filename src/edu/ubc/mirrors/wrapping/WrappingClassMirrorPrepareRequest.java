package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ClassMirrorPrepareRequest;

public class WrappingClassMirrorPrepareRequest extends WrappingMirrorEventRequest implements ClassMirrorPrepareRequest {

    public WrappingClassMirrorPrepareRequest(WrappingVirtualMachine vm, ClassMirrorPrepareRequest wrapped) {
	super(vm, wrapped);
    }

}
