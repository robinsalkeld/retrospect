package edu.ubc.mirrors.mirages;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.fieldmap.DirectArrayMirror;
import edu.ubc.mirrors.raw.NativeClassMirror;

public class ObjectArrayMirage implements ObjectArrayMirror {

    public final ObjectArrayMirror mirror;
    
    public ObjectArrayMirage(int length) {
        System.out.println("Constructing new " + getClass());
        Class<?> originalClass = ObjectMirage.getOriginalClass(getClass());
        this.mirror = new DirectArrayMirror(new NativeClassMirror<Object>(originalClass), length);
    }
    
    public ObjectArrayMirage(ObjectArrayMirror mirror) {
        this.mirror = mirror;
    }

    public ClassMirror<?> getClassMirror() {
        return mirror.getClassMirror();
    }

    public int length() {
        return mirror.length();
    }

    public ObjectMirror<?> get(int index) throws ArrayIndexOutOfBoundsException {
        return mirror.get(index);
    }

    public void set(int index, ObjectMirror<?> o) throws ArrayIndexOutOfBoundsException {
        mirror.set(index, o);
    }
}
