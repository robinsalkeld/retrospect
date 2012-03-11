package edu.ubc.mirrors.mutable;

import edu.ubc.mirrors.LongArrayMirror;
import edu.ubc.mirrors.ClassMirror;

public class MutableLongArrayMirror implements LongArrayMirror {

    private final LongArrayMirror immutableMirror;
    private final long[] values;
    
    public MutableLongArrayMirror(LongArrayMirror immutableMirror) {
        this.immutableMirror = immutableMirror;
        this.values = new long[immutableMirror.length()];
        for (int i = 0; i < values.length; i++) {
            values[i] = immutableMirror.getLong(i);
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
    public long getLong(int index) throws ArrayIndexOutOfBoundsException {
        return values[index];
    }

    @Override
    public void setLong(int index, long i) throws ArrayIndexOutOfBoundsException {
        values[index] = i;
    }
}
