package edu.ubc.mirrors;

public interface ClassMirror<T> {

    public Class<?> getMirroredClass();
    
    public FieldMirror getStaticField(String name) throws NoSuchFieldException;
    
}
