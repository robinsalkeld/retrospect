package edu.ubc.mirrors;


public interface FieldMirrorSetHandlerEvent extends MirrorEvent {

    public InstanceMirror target();
    public FieldMirror field();
    public Object newValue();
}
