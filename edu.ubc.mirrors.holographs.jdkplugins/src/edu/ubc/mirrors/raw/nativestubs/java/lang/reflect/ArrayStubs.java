package edu.ubc.mirrors.raw.nativestubs.java.lang.reflect;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class ArrayStubs extends NativeStubs {
    
    public ArrayStubs(ClassHolograph klass) {
	super(klass);
    }

    @StubMethod
    public ObjectMirror newArray(ClassMirror componentClassMirror, int length) {
        return componentClassMirror.newArray(length);
    }
    
    @StubMethod
    public int getLength(ObjectMirror array) {
        return ((ArrayMirror)array).length();
    }

    // TODO-RS: This should work for primitive arrays too! Need to explicitly box.
    @StubMethod
    public ObjectMirror get(ObjectMirror array, int index) {
        return ((ObjectArrayMirror)array).get(index);
    }
}
