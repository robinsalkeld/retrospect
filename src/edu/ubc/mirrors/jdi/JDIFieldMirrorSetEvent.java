package edu.ubc.mirrors.jdi;

import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.request.ModificationWatchpointRequest;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirrorSetEvent;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequest;

public class JDIFieldMirrorSetEvent extends JDIMirrorEvent implements FieldMirrorSetEvent {

    private final ModificationWatchpointEvent wrapped;
    
    public JDIFieldMirrorSetEvent(JDIVirtualMachineMirror vm, ModificationWatchpointEvent wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public InstanceMirror instance() {
	return (InstanceMirror)vm.makeMirror(wrapped.object());
    }

    @Override
    public ClassMirror classMirror() {
	return vm.makeClassMirror(wrapped.field().declaringType());
    }

    @Override
    public String fieldName() {
	return wrapped.field().name();
    }

    @Override
    public Object newValue() {
	return vm.wrapValue(wrapped.valueToBe());
    }

}
