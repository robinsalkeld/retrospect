package edu.ubc.mirrors;

import java.lang.reflect.InvocationTargetException;

public interface MethodMirror {
    public ObjectMirror invoke(InstanceMirror obj, ObjectMirror ... args) throws IllegalAccessException, InvocationTargetException;
}
