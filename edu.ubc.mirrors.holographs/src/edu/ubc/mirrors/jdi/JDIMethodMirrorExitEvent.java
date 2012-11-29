package edu.ubc.mirrors.jdi;

import com.sun.jdi.event.MethodExitEvent;

import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorExitEvent;
import edu.ubc.mirrors.ThreadMirror;

public class JDIMethodMirrorExitEvent extends JDIMirrorEvent implements MethodMirrorExitEvent {

    private final MethodExitEvent wrapped;
    
    public JDIMethodMirrorExitEvent(JDIVirtualMachineMirror vm, MethodExitEvent wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public ThreadMirror thread() {
        return (ThreadMirror)vm.makeMirror(wrapped.thread());
    }
    
    @Override
    public MethodMirror method() {
        return new JDIMethodMirror(vm, wrapped.method());
    }
    
    public static JDIMethodMirrorExitEvent wrap(JDIVirtualMachineMirror vm, MethodExitEvent mee) {
	JDIMethodMirrorExitEvent result = new JDIMethodMirrorExitEvent(vm, mee);
	Object request = mee.request().getProperty(JDIEventRequest.MIRROR_WRAPPER);
	if (!(request instanceof JDIMethodMirrorExitRequest)) {
	    return null;
	}
	JDIMethodMirrorExitRequest mmer = (JDIMethodMirrorExitRequest)request;
	// Apply the method filter if present, since it's not supported directly
	if (mmer.methodFilter != null && !mmer.methodFilter.equals(result.method())) {
	    return null;
	}
	return result;
    }

}
