package edu.ubc.mirrors;

public interface ClassMirror<T> {

    public String getClassName();
    
    public boolean isArray();
    
    public ClassMirror<?> getComponentClassMirror();
    
    public FieldMirror getStaticField(String name) throws NoSuchFieldException;
    
}
