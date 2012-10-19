package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorEntryEvent;

public class WrappingConstructorMirrorEntryEvent extends WrappingMirrorEvent implements ConstructorMirrorEntryEvent {

    private final ConstructorMirrorEntryEvent wrapped;
    
    public WrappingConstructorMirrorEntryEvent(WrappingVirtualMachine vm, ConstructorMirrorEntryEvent wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public ConstructorMirror constructor() {
	return vm.wrapConstructor(wrapped.constructor());
    }
}
