package edu.ubc.mirrors.jdi;

import com.sun.jdi.event.MethodEntryEvent;

import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorEntryEvent;

public class JDIConstructorMirrorEntryEvent extends JDIMirrorEvent implements ConstructorMirrorEntryEvent {

    private final MethodEntryEvent wrapped;
    
    public JDIConstructorMirrorEntryEvent(JDIVirtualMachineMirror vm, MethodEntryEvent wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public ConstructorMirror constructor() {
        return new JDIConstructorMirror(vm, wrapped.method());
    }
}
