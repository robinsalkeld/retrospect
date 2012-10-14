package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.MirrorEventRequest;

public class WrappingMirrorEventRequest implements MirrorEventRequest {
    protected final WrappingVirtualMachine vm;
    private final MirrorEventRequest wrapped;

    protected static final String WRAPPER = "edu.ubc.mirrors.wrapping.wrapper";
    
    public WrappingMirrorEventRequest(WrappingVirtualMachine vm, MirrorEventRequest wrapped) {
	this.vm = vm;
	this.wrapped = wrapped;
	wrapped.putProperty(WRAPPER, this);
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

    @Override
    public Object getProperty(Object key) {
	return wrapped.getProperty(key);
    }

    @Override
    public void putProperty(Object key, Object value) {
	wrapped.putProperty(key, value);
    }

    @Override
    public void addClassFilter(String classNamePattern) {
	wrapped.addClassFilter(classNamePattern);
    }
}
