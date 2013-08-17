package edu.ubc.mirrors.fieldmap;

import java.util.HashMap;
import java.util.Map;

import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.VirtualMachineMirror;

public class FieldMapMirrorEventRequest implements MirrorEventRequest {

    protected final VirtualMachineMirror vm;
    private boolean enabled = false;
    private final Map<Object, Object> properties = new HashMap<Object, Object>();
    
    public FieldMapMirrorEventRequest(VirtualMachineMirror vm) {
        this.vm = vm;
    }
    
    @Override
    public void enable() {
        enabled = true;
    }

    @Override
    public void disable() {
        enabled = false;
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

    @Override
    public void addClassFilter(String classNamePattern) {
        // Ignore for now
    }

}
