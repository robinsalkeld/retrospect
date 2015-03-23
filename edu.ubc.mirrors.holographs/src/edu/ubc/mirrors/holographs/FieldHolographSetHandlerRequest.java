package edu.ubc.mirrors.holographs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FieldMirrorSetHandlerRequest;

public class FieldHolographSetHandlerRequest implements FieldMirrorSetHandlerRequest {

    private final VirtualMachineHolograph vm;
    private boolean enabled = false;
    private final Map<Object, Object> properties = new HashMap<Object, Object>();
    private final FieldMirror fieldFilter;
    private final List<String> classNamePatterns = new ArrayList<String>();
    
    public FieldHolographSetHandlerRequest(VirtualMachineHolograph vm, FieldMirror field) {
        this.vm = vm;
        this.fieldFilter = field;
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

    @Override
    public void addClassFilter(String classNamePattern) {
        classNamePatterns.add(classNamePattern);
    }
    
}
