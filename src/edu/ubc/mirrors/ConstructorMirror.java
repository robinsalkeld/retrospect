package edu.ubc.mirrors;

import java.lang.reflect.InvocationTargetException;

public interface ConstructorMirror {
    // TODO-RS: Should we have a different InvocationTargetException that has a mirror as a cause instead?
    public InstanceMirror newInstance(Object ... args)
            throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException;

    public void setAccessible(boolean flag);
}
