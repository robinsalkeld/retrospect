package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;

public class NativeObjectArrayMirror extends NativeObjectMirror implements ObjectArrayMirror {
    
    private final Object[] array;
    
    public NativeObjectArrayMirror(Object[] array) {
        super(array);
        if (array == null) {
            throw new NullPointerException();
        }
        this.array = array;
    }

    public int length() {
        return array.length;
    }

    public ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException {
        return NativeInstanceMirror.makeMirror(array[index]);
    }

    public void set(int index, ObjectMirror o) throws ArrayIndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        return "NativeObjectArrayMirror: " + array;
    }
}
