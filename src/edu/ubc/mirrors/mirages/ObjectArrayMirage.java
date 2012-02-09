package edu.ubc.mirrors.mirages;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.raw.NativeObjectArrayMirror;

public class ObjectArrayMirage implements ArrayMirror {

    public final ArrayMirror mirror;
    
    public ObjectArrayMirage(Object[] array) {
        this.mirror = new NativeObjectArrayMirror(new NativeClassMirror<Object>(getClass()), array);
    }
    
    public ObjectArrayMirage(ArrayMirror mirror) {
        this.mirror = mirror;
    }

    public ClassMirror<?> getClassMirror() {
        return mirror.getClassMirror();
    }

    public int length() {
        return mirror.length();
    }
}
