package edu.ubc.mirrors;

public interface ObjectMirror<T> {
    
    // TODO: Refactor into ClassMirror interface instead
    public FieldMirror getMemberField(String name) throws NoSuchFieldException;
    public FieldMirror getStaticField(String name) throws NoSuchFieldException;
    
    public Class<? extends T> getClassMirror();
}
