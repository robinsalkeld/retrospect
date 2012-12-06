package edu.ubc.mirrors;

public interface FieldMirror {

    public ClassMirror getDeclaringClass();
    public String getName();
    public String getTypeName();
    public ClassMirror getType();
    public int getModifiers();
    
}
