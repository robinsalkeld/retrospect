package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;

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

    public ObjectMirror<?> get(int index) throws ArrayIndexOutOfBoundsException {
        return NativeObjectMirror.makeMirror(array[index]);
    }

    public void set(int index, ObjectMirror<?> o) throws ArrayIndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }
}
