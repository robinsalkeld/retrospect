package edu.ubc.mirrors;

import java.util.List;

public interface MethodMirror {
    
    public ClassMirror getDeclaringClass();
    
    public int getSlot();
    
    public int getModifiers();
    
    public String getName();
    public List<String> getParameterTypeNames();
    public List<ClassMirror> getParameterTypes();
    public List<String> getExceptionTypeNames();
    public List<ClassMirror> getExceptionTypes();
    public String getReturnTypeName();
    public ClassMirror getReturnType();
    public String getSignature();
    
    // TODO-RS: Should we have a different InvocationTargetException that has a mirror as a cause instead?
    public Object invoke(ThreadMirror thread, ObjectMirror obj, Object ... args) throws IllegalArgumentException, IllegalAccessException, MirrorInvocationTargetException;
    
    public void setAccessible(boolean flag);
    
    public byte[] getRawAnnotations();
    public byte[] getRawParameterAnnotations();
    public byte[] getRawAnnotationDefault();
}