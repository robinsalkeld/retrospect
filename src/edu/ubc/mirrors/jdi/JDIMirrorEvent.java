package edu.ubc.mirrors.jdi;

import com.sun.jdi.event.Event;

import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequest;

public class JDIMirrorEvent extends JDIMirror implements MirrorEvent {

    private final Event wrapped;
    
    public JDIMirrorEvent(JDIVirtualMachineMirror vm, Event wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public MirrorEventRequest request() {
        return (MirrorEventRequest)wrapped.request().getProperty(JDIEventRequest.MIRROR_WRAPPER);
    }
}
