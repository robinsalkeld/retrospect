package edu.ubc.mirrors;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface MethodMirror {
    
    String getName();
    List<ClassMirror> getParameterTypes();
    ClassMirror getReturnType();
    
    // TODO-RS: Should we have a different InvocationTargetException that has a mirror as a cause instead?
    public Object invoke(ThreadMirror thread, ObjectMirror obj, Object ... args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
    
    public void setAccessible(boolean flag);
}
