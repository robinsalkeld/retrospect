package edu.ubc.mirrors;

public interface ObjectMirror {
    public ClassMirror getClassMirror();
    
    public int identityHashCode();
//    public VirtualMachineMirror getVM();
}
