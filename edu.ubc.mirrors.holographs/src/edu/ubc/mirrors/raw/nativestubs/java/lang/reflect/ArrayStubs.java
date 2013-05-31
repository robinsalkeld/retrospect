package edu.ubc.mirrors.raw.nativestubs.java.lang.reflect;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.NativeStubs;

public class ArrayStubs extends NativeStubs {
    
    public ArrayStubs(ClassHolograph klass) {
	super(klass);
    }

    public ObjectMirror newArray(ClassMirror componentClassMirror, int length) {
        return componentClassMirror.newArray(length);
    }
    
    public int getLength(ObjectMirror array) {
        return ((ArrayMirror)array).length();
    }

    // TODO-RS: This should work for primitive arrays too! Need to explicitly box.
    public ObjectMirror get(ObjectMirror array, int index) {
        return ((ObjectArrayMirror)array).get(index);
    }
}
