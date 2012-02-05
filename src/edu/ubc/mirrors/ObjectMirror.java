package edu.ubc.mirrors;

public interface ObjectMirror<T> {
    
    // TODO: Refactor into ClassMirror interface instead
    public FieldMirror getMemberField(String name) throws NoSuchFieldException;
    
    public ClassMirror<?> getClassMirror();
}
