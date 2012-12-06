package edu.ubc.mirrors;

import java.util.List;

public interface ConstructorMirror {
    
    public ClassMirror getDeclaringClass();
    
    public int getSlot();
    
    public int getModifiers();
    
    public List<String> getParameterTypeNames();
    public List<ClassMirror> getParameterTypes();
    public List<String> getExceptionTypeNames();
    public List<ClassMirror> getExceptionTypes();
    
    public String getSignature();
    
    public byte[] getRawAnnotations();
    
    public byte[] getRawParameterAnnotations();
    
    public InstanceMirror newInstance(ThreadMirror thread, Object ... args)
            throws IllegalAccessException, IllegalArgumentException, MirrorInvocationTargetException;
}
