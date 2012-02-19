package edu.ubc.mirrors;

public interface InstanceMirror extends ObjectMirror {

    // TODO: Refactor into ClassMirror interface instead
    public FieldMirror getMemberField(String name) throws NoSuchFieldException;
}
