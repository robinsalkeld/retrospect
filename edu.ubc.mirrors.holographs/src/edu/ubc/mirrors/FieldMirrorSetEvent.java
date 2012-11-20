package edu.ubc.mirrors;

public interface FieldMirrorSetEvent extends MirrorEvent {

    public InstanceMirror instance();
    public ClassMirror classMirror();
    public String fieldName();
    public Object newValue();
    
}
