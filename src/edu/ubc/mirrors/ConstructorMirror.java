package edu.ubc.mirrors;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface ConstructorMirror {
    
    public ClassMirror getDeclaringClass();
    
    public int getSlot();
    
    public int getModifiers();
    
    public List<ClassMirror> getParameterTypes();
    
    public List<ClassMirror> getExceptionTypes();
    
    public String getSignature();
    
    public byte[] getAnnotations();
    
    public byte[] getParameterAnnotations();
    
    // TODO-RS: Should we have a different InvocationTargetException that has a mirror as a cause instead?
    public InstanceMirror newInstance(Object ... args)
            throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException;
}
