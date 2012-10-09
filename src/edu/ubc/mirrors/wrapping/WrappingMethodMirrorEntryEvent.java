package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryEvent;

public class WrappingMethodMirrorEntryEvent extends WrappingMirrorEvent implements MethodMirrorEntryEvent {

    private final MethodMirrorEntryEvent wrapped;
    
    public WrappingMethodMirrorEntryEvent(WrappingVirtualMachine vm, MethodMirrorEntryEvent wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public MethodMirror method() {
	return new WrappingMethodMirror(vm, wrapped.method());
    }
}
