package edu.ubc.mirrors.mutable;

import edu.ubc.mirrors.IntArrayMirror;
import edu.ubc.mirrors.ClassMirror;

public class MutableIntArrayMirror implements IntArrayMirror {

    private final IntArrayMirror immutableMirror;
    private final int[] values;
    
    public MutableIntArrayMirror(IntArrayMirror immutableMirror) {
        this.immutableMirror = immutableMirror;
        this.values = new int[immutableMirror.length()];
        for (int i = 0; i < values.length; i++) {
            values[i] = immutableMirror.getInt(i);
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
    public int getInt(int index) throws ArrayIndexOutOfBoundsException {
        return values[index];
    }

    @Override
    public void setInt(int index, int i) throws ArrayIndexOutOfBoundsException {
        values[index] = i;
    }
}
