package edu.ubc.mirrors;

import java.io.InputStream;
import java.util.List;


public interface ClassMirror {

    public String getClassName();
    
    public InputStream getBytecodeStream();
    
    public boolean isArray();
    
    public ClassMirror getComponentClassMirror();
    
    public ClassMirror getSuperClassMirror();
    
    public boolean isInterface();
    
    public List<ClassMirror> getInterfaceMirrors();
    
    public FieldMirror getStaticField(String name) throws NoSuchFieldException;
    
}
