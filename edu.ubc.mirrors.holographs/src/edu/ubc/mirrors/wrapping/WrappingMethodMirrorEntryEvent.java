package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryEvent;
import edu.ubc.mirrors.ThreadMirror;

public class WrappingMethodMirrorEntryEvent extends WrappingMirrorEvent implements MethodMirrorEntryEvent {

    private final MethodMirrorEntryEvent wrapped;
    
    public WrappingMethodMirrorEntryEvent(WrappingVirtualMachine vm, MethodMirrorEntryEvent wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public ThreadMirror thread() {
        return (ThreadMirror)vm.wrapMirror(wrapped.thread());
    }
    
    @Override
    public MethodMirror method() {
	return vm.wrapMethod(wrapped.method());
    }
}
