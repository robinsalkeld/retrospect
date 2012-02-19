package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;

public class NativeByteArrayMirror implements ByteArrayMirror {

    private final byte[] array;
    
    public NativeByteArrayMirror(byte[] array) {
        this.array = array;
    }
    
    public int length() {
        return array.length;
    }

    public ClassMirror getClassMirror() {
        return new NativeClassMirror(array.getClass());
    }

    public byte getByte(int index) throws ArrayIndexOutOfBoundsException {
        return array[index];
    }

    public void setByte(int index, byte b) throws ArrayIndexOutOfBoundsException {
        array[index] = b;
    }

}
