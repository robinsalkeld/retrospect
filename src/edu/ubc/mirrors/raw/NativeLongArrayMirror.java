package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.LongArrayMirror;
import edu.ubc.mirrors.ClassMirror;

public class NativeLongArrayMirror implements LongArrayMirror {

    private final long[] array;
    
    public NativeLongArrayMirror(long[] array) {
        this.array = array;
    }
    
    public int length() {
        return array.length;
    }

    public ClassMirror getClassMirror() {
        return new NativeClassMirror(array.getClass());
    }

    public long getLong(int index) throws ArrayIndexOutOfBoundsException {
        return array[index];
    }

    public void setLong(int index, long l) throws ArrayIndexOutOfBoundsException {
        array[index] = l;
    }

}
