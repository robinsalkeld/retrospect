package edu.ubc.mirrors;

import java.util.List;

public interface ClassMirrorLoader extends InstanceMirror {
    
    public List<ClassMirror> loadedClassMirrors();
    
    public ClassMirror findLoadedClassMirror(String name);
    
    public ClassMirror defineClass(String name, ByteArrayMirror b, int off, int len,
            InstanceMirror pd, InstanceMirror source, boolean unsafe);
}
