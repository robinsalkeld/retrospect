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
    
    public static JDIConstructorMirrorEntryEvent wrap(JDIVirtualMachineMirror vm, MethodEntryEvent mee) {
	JDIConstructorMirrorEntryEvent result = new JDIConstructorMirrorEntryEvent(vm, mee);
	Object request = mee.request().getProperty(JDIEventRequest.MIRROR_WRAPPER);
	if (!(request instanceof JDIConstructorMirrorEntryRequest)) {
	    return null;
	}
	JDIConstructorMirrorEntryRequest cmer = (JDIConstructorMirrorEntryRequest)request;
	// Apply the constructor filter if present, since it's not supported directly
	if (cmer.constructorFilter != null && !cmer.constructorFilter.equals(result.constructor())) {
	    return null;
	}
	return result;
    }

}
