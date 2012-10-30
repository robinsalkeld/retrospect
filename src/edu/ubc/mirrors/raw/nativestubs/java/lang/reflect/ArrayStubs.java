package edu.ubc.mirrors.raw.nativestubs.java.lang.reflect;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.ObjectMirage;

public class ArrayStubs extends NativeStubs {
    
    public ArrayStubs(ClassHolograph klass) {
	super(klass);
    }

    public Mirage newArray(Mirage componentType, int length) {
        ClassMirror componentClassMirror = (ClassMirror)componentType.getMirror();
        ArrayMirror result = componentClassMirror.newArray(length);
        return ObjectMirage.make(result);
    }
    
    public int getLength(Mirage array) {
        return ((ArrayMirror)array.getMirror()).length();
    }
    
    public Mirage get(Mirage array, int index) {
        ObjectMirror mirror = ((ObjectArrayMirror)array.getMirror()).get(index);
        return (Mirage)ObjectMirage.make(mirror);
    }
}
