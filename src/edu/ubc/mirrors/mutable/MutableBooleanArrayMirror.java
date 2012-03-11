package edu.ubc.mirrors.mutable;

import edu.ubc.mirrors.BooleanArrayMirror;
import edu.ubc.mirrors.ClassMirror;

public class MutableBooleanArrayMirror implements BooleanArrayMirror {

    private final BooleanArrayMirror immutableMirror;
    private final boolean[] values;
    
    public MutableBooleanArrayMirror(BooleanArrayMirror immutableMirror) {
        this.immutableMirror = immutableMirror;
        this.values = new boolean[immutableMirror.length()];
        for (int i = 0; i < values.length; i++) {
            values[i] = immutableMirror.getBoolean(i);
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
    public boolean getBoolean(int index) throws ArrayIndexOutOfBoundsException {
        return values[index];
    }

    @Override
    public void setBoolean(int index, boolean b) throws ArrayIndexOutOfBoundsException {
        values[index] = b;
    }
}
