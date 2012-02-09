package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.IntArrayMirror;
import edu.ubc.mirrors.ClassMirror;

public class NativeIntArrayMirror implements IntArrayMirror {

    private final int[] array;
    
    public NativeIntArrayMirror(int[] array) {
        this.array = array;
    }
    
    public int length() {
        return array.length;
    }

    public ClassMirror<?> getClassMirror() {
        return new NativeClassMirror<Object>(array.getClass());
    }

    public int getInt(int index) throws ArrayIndexOutOfBoundsException {
        return array[index];
    }

    public void setInt(int index, int i) throws ArrayIndexOutOfBoundsException {
        array[index] = i;
    }

}
