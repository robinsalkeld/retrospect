package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorPrepareEvent;

public class WrappingClassMirrorPrepareEvent extends WrappingMirrorEvent implements ClassMirrorPrepareEvent {

    private final ClassMirrorPrepareEvent wrapped;
    
    public WrappingClassMirrorPrepareEvent(WrappingVirtualMachine vm, ClassMirrorPrepareEvent wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public ClassMirror classMirror() {
	return (ClassMirror)vm.getWrappedMirror(wrapped.classMirror());
    }
}
