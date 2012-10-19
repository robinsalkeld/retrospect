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
    
    public static JDIMethodMirrorEntryEvent wrap(JDIVirtualMachineMirror vm, MethodEntryEvent wrapped) {
	Object request = wrapped.request().getProperty(JDIEventRequest.MIRROR_WRAPPER);
	if (!(request instanceof JDIMethodMirrorEntryRequest)) {
	    return null;
	}
	JDIMethodMirrorEntryEvent result = new JDIMethodMirrorEntryEvent(vm, wrapped);
	JDIMethodMirrorEntryRequest mmer = (JDIMethodMirrorEntryRequest)request;
	// Apply the method filter if present, since it's not supported directly
	if (mmer.methodFilter != null && !mmer.methodFilter.equals(result.method())) {
	    return null;
	}
	return result;
    }
}
