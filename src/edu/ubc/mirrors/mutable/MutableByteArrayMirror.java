package edu.ubc.mirrors.mutable;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;

public class MutableByteArrayMirror implements ByteArrayMirror {

    private final ByteArrayMirror immutableMirror;
    private final byte[] values;
    
    public MutableByteArrayMirror(ByteArrayMirror immutableMirror) {
        this.immutableMirror = immutableMirror;
        this.values = new byte[immutableMirror.length()];
        for (int i = 0; i < values.length; i++) {
            values[i] = immutableMirror.getByte(i);
        }
    }
    
    @Override
    public ClassMirror getClassMirror() {
        return immutableMirror.getClassMirror();
    }

    @Override
    public int length() {
        return immutableMirror.length();
    }

    @Override
    public byte getByte(int index) throws ArrayIndexOutOfBoundsException {
        return values[index];
    }

    @Override
    public void setByte(int index, byte b) throws ArrayIndexOutOfBoundsException {
        values[index] = b;
    }
}
