package edu.ubc.mirrors.mirages;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.fieldmap.DirectArrayMirror;
import edu.ubc.mirrors.raw.NativeClassMirror;

public class ObjectArrayMirage extends ObjectMirage implements ObjectArrayMirror {

    public final ObjectArrayMirror mirror;
    
    public ObjectArrayMirage(int length) {
        super(null);
        Class<?> originalClass = ObjectMirage.getOriginalClass(getClass());
        this.mirror = new DirectArrayMirror(new NativeClassMirror(originalClass), length);
    }
    
    public ObjectArrayMirage(ObjectArrayMirror mirror) {
        super(mirror);
        this.mirror = mirror;
    }

    public ClassMirror getClassMirror() {
        return mirror.getClassMirror();
    }

    public int length() {
        return mirror.length();
    }

    public ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException {
        return mirror.get(index);
    }

    public void set(int index, ObjectMirror o) throws ArrayIndexOutOfBoundsException {
        mirror.set(index, o);
    }
}
