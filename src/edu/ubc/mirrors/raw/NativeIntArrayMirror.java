package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.IntArrayMirror;

public class NativeIntArrayMirror extends NativeObjectMirror implements IntArrayMirror {

    private final int[] array;
    
    public NativeIntArrayMirror(int[] array) {
        super(array);
        this.array = array;
    }
    
    public int length() {
        return array.length;
    }

    public int getInt(int index) throws ArrayIndexOutOfBoundsException {
        return array[index];
    }

    public void setInt(int index, int i) throws ArrayIndexOutOfBoundsException {
        array[index] = i;
    }

}
