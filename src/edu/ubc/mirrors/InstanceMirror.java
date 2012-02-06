package edu.ubc.mirrors;

public interface InstanceMirror<T> extends ObjectMirror<T> {

    // TODO: Refactor into ClassMirror interface instead
    public FieldMirror getMemberField(String name) throws NoSuchFieldException;
}
