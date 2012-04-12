package edu.ubc.mirrors;

import java.util.List;

public interface VirtualMachineMirror {

    public ClassMirror findBootstrapClassMirror(String name);
    
    public List<ClassMirror> findAllClasses(String name); 
}
