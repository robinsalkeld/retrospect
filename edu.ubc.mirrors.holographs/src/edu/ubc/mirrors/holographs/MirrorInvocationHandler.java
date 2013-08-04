package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;

public interface MirrorInvocationHandler {

    public Object invoke(InstanceMirror proxy, MethodMirror method, Object[] args) 
            throws MirrorInvocationTargetException;

}
