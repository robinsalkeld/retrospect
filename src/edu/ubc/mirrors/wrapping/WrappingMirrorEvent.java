package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.MirrorEvent;

public class WrappingMirrorEvent implements MirrorEvent {

    protected final WrappingVirtualMachine vm;
    private final MirrorEvent wrapped;
    
    public WrappingMirrorEvent(WrappingVirtualMachine vm, MirrorEvent wrapped) {
	super();
	this.vm = vm;
	this.wrapped = wrapped;
    }
}
