package edu.ubc.mirrors.jdi;

import java.util.HashMap;
import java.util.Map;

import com.sun.jdi.request.EventRequest;

import edu.ubc.mirrors.MirrorEventRequest;

public abstract class JDIEventRequest extends JDIMirror implements MirrorEventRequest {
    protected final EventRequest wrapped;
    private final Map<Object, Object> properties = new HashMap<Object, Object>();
    
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
    public boolean isEnabled() {
        return wrapped.isEnabled();
    }
    
    @Override
    public void setEnabled(boolean enabled) {
	wrapped.setEnabled(enabled);
    }

    @Override
    public Object getProperty(Object key) {
	return properties.get(key);
    }

    @Override
    public void putProperty(Object key, Object value) {
	properties.put(key, value);
    }

    
}
