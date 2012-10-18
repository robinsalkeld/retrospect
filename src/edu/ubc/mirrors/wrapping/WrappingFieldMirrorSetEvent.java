package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirrorSetEvent;
import edu.ubc.mirrors.InstanceMirror;

public class WrappingFieldMirrorSetEvent extends WrappingMirrorEvent implements FieldMirrorSetEvent {

    private final FieldMirrorSetEvent wrapped;
    
    public WrappingFieldMirrorSetEvent(WrappingVirtualMachine vm, FieldMirrorSetEvent wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public InstanceMirror instance() {
	return (InstanceMirror)vm.wrapMirror(wrapped.instance());
    }

    @Override
    public ClassMirror classMirror() {
	return (ClassMirror)vm.wrapMirror(wrapped.classMirror());
    }

    @Override
    public String fieldName() {
	return wrapped.fieldName();
    }

    @Override
    public Object newValue() {
	return vm.wrapValue(wrapped.newValue());
    }
}
