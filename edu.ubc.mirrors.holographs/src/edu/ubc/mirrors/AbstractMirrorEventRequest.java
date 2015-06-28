package edu.ubc.mirrors;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractMirrorEventRequest implements MirrorEventRequest {

    protected final VirtualMachineMirror vm;
    private boolean enabled = false;
    private final Map<Object, Object> properties = new HashMap<Object, Object>();
    
    public AbstractMirrorEventRequest(VirtualMachineMirror vm) {
        this.vm = vm;
    }
    
    @Override
    public void enable() {
        setEnabled(true);
    }

    @Override
    public void disable() {
        setEnabled(false);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
