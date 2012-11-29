package edu.ubc.mirrors;


public interface MirrorEventRequest {

    public void enable();
    public void disable();
    public boolean isEnabled();
    public void setEnabled(boolean enabled);
    public Object getProperty(Object key);
    public void putProperty(Object key, Object value);
    
    public void addClassFilter(String classNamePattern);
}
