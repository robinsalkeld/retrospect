package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorExitEvent;

public class WrappingConstructorMirrorExitEvent extends WrappingMirrorEvent implements ConstructorMirrorExitEvent {

    private final ConstructorMirrorExitEvent wrapped;
    
    public WrappingConstructorMirrorExitEvent(WrappingVirtualMachine vm, ConstructorMirrorExitEvent wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public ConstructorMirror constructor() {
	return vm.wrapConstructor(wrapped.constructor());
    }
}
