package edu.ubc.mirrors;



public interface ClassMirrorLoader extends InstanceMirror {
    
    public ClassMirror findLoadedClassMirror(String name);
    
    public ClassMirror defineClass1(String name, ByteArrayMirror b, int off, int len,
            InstanceMirror pd, InstanceMirror source, boolean verify);
}
