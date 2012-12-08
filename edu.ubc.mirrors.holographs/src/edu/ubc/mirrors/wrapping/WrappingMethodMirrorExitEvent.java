package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorExitEvent;
import edu.ubc.mirrors.ThreadMirror;

public class WrappingMethodMirrorExitEvent extends WrappingMirrorEvent implements MethodMirrorExitEvent {

    private final MethodMirrorExitEvent wrapped;
    
    public WrappingMethodMirrorExitEvent(WrappingVirtualMachine vm, MethodMirrorExitEvent wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public ThreadMirror thread() {
        return (ThreadMirror)vm.getWrappedMirror(wrapped.thread());
    }
    
    @Override
    public MethodMirror method() {
	return vm.wrapMethod(wrapped.method());
    }
}
