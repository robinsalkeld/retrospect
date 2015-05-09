package edu.ubc.mirrors;


public interface FieldMirrorGetHandlerEvent extends MirrorEvent {

    public InstanceMirror target();
    public FieldMirror field();
}
