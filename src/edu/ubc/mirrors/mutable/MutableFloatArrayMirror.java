package edu.ubc.mirrors.mutable;

import edu.ubc.mirrors.FloatArrayMirror;
import edu.ubc.mirrors.ClassMirror;

public class MutableFloatArrayMirror implements FloatArrayMirror {

    private final FloatArrayMirror immutableMirror;
    private final float[] values;
    
    public MutableFloatArrayMirror(FloatArrayMirror immutableMirror) {
        this.immutableMirror = immutableMirror;
        this.values = new float[immutableMirror.length()];
        for (int i = 0; i < values.length; i++) {
            values[i] = immutableMirror.getFloat(i);
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
    public float getFloat(int index) throws ArrayIndexOutOfBoundsException {
        return values[index];
    }

    @Override
    public void setFloat(int index, float i) throws ArrayIndexOutOfBoundsException {
        values[index] = i;
    }
}
