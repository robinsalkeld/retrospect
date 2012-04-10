package edu.ubc.mirrors;



public interface ClassMirrorLoader extends InstanceMirror {
    
    public ClassMirror findLoadedClassMirror(String name);
}
