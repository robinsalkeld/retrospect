package edu.ubc.mirrors.jdi;

import com.sun.jdi.request.EventRequest;

import edu.ubc.mirrors.MirrorEventRequest;

public class JDIEventRequest extends JDIMirror implements MirrorEventRequest {
    protected final EventRequest wrapped;
    
    protected static final String MIRROR_WRAPPER = "edu.ubc.mirrors.jdi.mirrorWrapper";
    
    public JDIEventRequest(JDIVirtualMachineMirror vm, EventRequest wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
	wrapped.putProperty(MIRROR_WRAPPER, this);
    }

    @Override
    public void enable() {
	wrapped.enable();
    }

    @Override
    public void disable() {
	wrapped.disable();
    }

    @Override
    public void setEnabled(boolean enabled) {
	wrapped.setEnabled(enabled);
    }
    
}
