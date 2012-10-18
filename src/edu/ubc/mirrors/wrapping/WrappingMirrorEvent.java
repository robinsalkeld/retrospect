package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequest;

public class WrappingMirrorEvent implements MirrorEvent {

    protected final WrappingVirtualMachine vm;
    private final MirrorEvent wrapped;
    
    public WrappingMirrorEvent(WrappingVirtualMachine vm, MirrorEvent wrapped) {
	this.vm = vm;
	this.wrapped = wrapped;
    }

    @Override
    public MirrorEventRequest request() {
	return (MirrorEventRequest)wrapped.request().getProperty(WrappingMirrorEventRequest.WRAPPER);
    }
}
