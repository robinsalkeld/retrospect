package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.ShortArrayMirror;
import edu.ubc.mirrors.ClassMirror;

public class NativeShortArrayMirror implements ShortArrayMirror {

    private final short[] array;
    
    public NativeShortArrayMirror(short[] array) {
        this.array = array;
    }
    
    public int length() {
        return array.length;
    }

    public ClassMirror getClassMirror() {
        return new NativeClassMirror(array.getClass());
    }

    public short getShort(int index) throws ArrayIndexOutOfBoundsException {
        return array[index];
    }

    public void setShort(int index, short s) throws ArrayIndexOutOfBoundsException {
        array[index] = s;
    }

}
