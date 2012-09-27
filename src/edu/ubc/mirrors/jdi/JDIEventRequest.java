package edu.ubc.mirrors.jdi;

import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.MethodEntryRequest;

import edu.ubc.mirrors.MirrorEventRequest;

public class JDIEventRequest implements MirrorEventRequest {
    private final JDIVirtualMachineMirror vm;
    protected final EventRequest wrapped;
    
    protected static final String MIRROR_WRAPPER = "edu.ubc.mirrors.jdi.mirrorWrapper";
    
    
    public JDIEventRequest(JDIVirtualMachineMirror vm, MethodEntryRequest wrapped) {
	this.vm = vm;
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
