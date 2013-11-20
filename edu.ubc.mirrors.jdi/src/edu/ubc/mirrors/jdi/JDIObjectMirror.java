package edu.ubc.mirrors.jdi;

import com.sun.jdi.ObjectReference;

import edu.ubc.mirrors.ObjectMirror;

public interface JDIObjectMirror extends ObjectMirror {

    public ObjectReference getObjectReference();
}
