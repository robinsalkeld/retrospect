package edu.ubc.mirrors;

public interface VirtualMachineMirror {

    public abstract ClassMirror findBootstrapClassMirror(String name);
    
}
