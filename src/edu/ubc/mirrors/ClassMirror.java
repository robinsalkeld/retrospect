package edu.ubc.mirrors;

public interface ClassMirror<T> {

    public String getClassName();
    
    public FieldMirror getStaticField(String name) throws NoSuchFieldException;
    
}
