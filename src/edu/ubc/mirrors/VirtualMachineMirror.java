package edu.ubc.mirrors;

import java.util.List;

public interface VirtualMachineMirror {

    public ClassMirror findBootstrapClassMirror(String name);
    
    public List<ClassMirror> findAllClasses(String name); 
    
    public ClassMirror getPrimitiveClass(String name);
    
    public ClassMirror getArrayClass(int dimensions, ClassMirror elementClass);
}
