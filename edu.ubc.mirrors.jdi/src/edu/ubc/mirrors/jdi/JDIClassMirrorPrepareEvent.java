package edu.ubc.mirrors.jdi;

import com.sun.jdi.event.ClassPrepareEvent;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorPrepareEvent;

public class JDIClassMirrorPrepareEvent extends JDIMirrorEvent implements ClassMirrorPrepareEvent {

    private final ClassPrepareEvent wrapped;
    
    public JDIClassMirrorPrepareEvent(JDIVirtualMachineMirror vm, ClassPrepareEvent wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public ClassMirror classMirror() {
	return vm.makeClassMirror(wrapped.referenceType());
    }
}
