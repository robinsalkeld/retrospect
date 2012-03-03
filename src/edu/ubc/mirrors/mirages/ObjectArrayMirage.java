package edu.ubc.mirrors.mirages;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.fieldmap.DirectArrayMirror;
import edu.ubc.mirrors.raw.NativeClassMirror;

public class ObjectArrayMirage extends ObjectMirage implements ObjectArrayMirror {

    public final ObjectArrayMirror arrayMirror;
    
    public ObjectArrayMirage(int length) {
        super(null);
        Class<?> originalClass = ObjectMirage.getOriginalClass(getClass());
        this.arrayMirror = new DirectArrayMirror(new NativeClassMirror(originalClass), length);
        this.mirror = arrayMirror;
    }
    
    public ObjectArrayMirage(ObjectArrayMirror mirror) {
        super(mirror);
        this.arrayMirror = mirror;
    }

    public ClassMirror getClassMirror() {
        return mirror.getClassMirror();
    }

    public int length() {
        return arrayMirror.length();
    }

    public ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException {
        return arrayMirror.get(index);
    }

    public void set(int index, ObjectMirror o) throws ArrayIndexOutOfBoundsException {
        arrayMirror.set(index, o);
    }
}
