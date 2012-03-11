package edu.ubc.mirrors.mutable;

import edu.ubc.mirrors.ShortArrayMirror;
import edu.ubc.mirrors.ClassMirror;

public class MutableShortArrayMirror implements ShortArrayMirror {

    private final ShortArrayMirror immutableMirror;
    private final short[] values;
    
    public MutableShortArrayMirror(ShortArrayMirror immutableMirror) {
        this.immutableMirror = immutableMirror;
        this.values = new short[immutableMirror.length()];
        for (int i = 0; i < values.length; i++) {
            values[i] = immutableMirror.getShort(i);
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
    public short getShort(int index) throws ArrayIndexOutOfBoundsException {
        return values[index];
    }

    @Override
    public void setShort(int index, short s) throws ArrayIndexOutOfBoundsException {
        values[index] = s;
    }
}
