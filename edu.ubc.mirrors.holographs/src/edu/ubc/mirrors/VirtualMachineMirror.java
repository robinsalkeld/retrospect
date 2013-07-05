package edu.ubc.mirrors;

import java.util.List;

public interface VirtualMachineMirror {

    public ClassMirror findBootstrapClassMirror(String name);
    
    public ClassMirror defineBootstrapClass(String name, ByteArrayMirror b, int off, int len);
    
    public List<ClassMirror> findAllClasses();
    public List<ClassMirror> findAllClasses(String name, boolean includeSubclasses); 
    
    public List<ThreadMirror> getThreads();
    
    public ClassMirror getPrimitiveClass(String name);
    
    public ClassMirror getArrayClass(int dimensions, ClassMirror elementClass);
    
    public MirrorEventRequestManager eventRequestManager();
    
    public MirrorEventQueue eventQueue();
    
    public void suspend();
    public void resume();
    
    public boolean canBeModified();
    // This is also interpreted to mean "can get fields/methods/etc"
    public boolean canGetBytecodes();
    public boolean hasClassInitialization();

}
