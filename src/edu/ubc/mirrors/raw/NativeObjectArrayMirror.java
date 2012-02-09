package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectArrayMirror;

public class NativeObjectArrayMirror implements ObjectArrayMirror {
    
    private final Object[] array;
    
    public NativeObjectArrayMirror(Object[] array) {
        this.array = array;
    }

    public int length() {
        return array.length;
    }

    public ClassMirror<?> getClassMirror() {
        return new NativeClassMirror<Object[]>(array.getClass());
    }

    public Object get(int index) throws ArrayIndexOutOfBoundsException {
        return array[index];
    }

    public void set(int index, Object o) throws ArrayIndexOutOfBoundsException {
        array[index] = o;
    }
}
