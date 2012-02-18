package edu.ubc.mirrors;

import java.io.InputStream;


public interface ClassMirror<T> {

    public String getClassName();
    
    public InputStream getBytecodeStream();
    
    public boolean isArray();
    
    public ClassMirror<?> getComponentClassMirror();
    
    public ClassMirror<?> getSuperClassMirror();
    
    public FieldMirror getStaticField(String name) throws NoSuchFieldException;
    
}
