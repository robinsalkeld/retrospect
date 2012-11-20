package edu.ubc.mirrors.jdi;

import com.sun.jdi.event.MethodExitEvent;

import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorExitEvent;

public class JDIConstructorMirrorExitEvent extends JDIMirrorEvent implements ConstructorMirrorExitEvent {

    private final MethodExitEvent wrapped;
    
    public JDIConstructorMirrorExitEvent(JDIVirtualMachineMirror vm, MethodExitEvent wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public ConstructorMirror constructor() {
        return new JDIConstructorMirror(vm, wrapped.method());
    }
    
    public static JDIConstructorMirrorExitEvent wrap(JDIVirtualMachineMirror vm, MethodExitEvent mee) {
	JDIConstructorMirrorExitEvent result = new JDIConstructorMirrorExitEvent(vm, mee);
	Object request = mee.request().getProperty(JDIEventRequest.MIRROR_WRAPPER);
	if (!(request instanceof JDIConstructorMirrorExitRequest)) {
	    return null;
	}
	JDIConstructorMirrorExitRequest cmer = (JDIConstructorMirrorExitRequest)request;
	// Apply the method filter if present, since it's not supported directly
	if (cmer.constructorFilter != null && !cmer.constructorFilter.equals(result.constructor())) {
	    return null;
	}
	return result;
    }

}
