package edu.ubc.mirrors.mutable;

import edu.ubc.mirrors.DoubleArrayMirror;
import edu.ubc.mirrors.ClassMirror;

public class MutableDoubleArrayMirror implements DoubleArrayMirror {

    private final DoubleArrayMirror immutableMirror;
    private final double[] values;
    
    public MutableDoubleArrayMirror(DoubleArrayMirror immutableMirror) {
        this.immutableMirror = immutableMirror;
        this.values = new double[immutableMirror.length()];
        for (int i = 0; i < values.length; i++) {
            values[i] = immutableMirror.getDouble(i);
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
    public double getDouble(int index) throws ArrayIndexOutOfBoundsException {
        return values[index];
    }

    @Override
    public void setDouble(int index, double i) throws ArrayIndexOutOfBoundsException {
        values[index] = i;
    }
}
