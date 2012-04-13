package edu.ubc.mirrors;

import java.lang.reflect.InvocationTargetException;

public interface MethodMirror {
    // TODO-RS: Should we have a different InvocationTargetException that has a mirror as a cause instead?
    public ObjectMirror invoke(InstanceMirror obj, ObjectMirror ... args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
}
