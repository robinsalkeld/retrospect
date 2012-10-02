package edu.ubc.mirrors.jdi;

import com.sun.jdi.event.MethodEntryEvent;

import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryEvent;

public class JDIMethodMirrorEntryEvent extends JDIMirrorEvent implements MethodMirrorEntryEvent {

    private final MethodEntryEvent wrapped;
    
    public JDIMethodMirrorEntryEvent(JDIVirtualMachineMirror vm, MethodEntryEvent wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public MethodMirror method() {
        return new JDIMethodMirror(vm, wrapped.method());
    }
}
