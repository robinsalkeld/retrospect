package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.DoubleArrayMirror;
import edu.ubc.mirrors.wrapping.WrappingDoubleArrayMirror;

public class MutableDoubleArrayMirror extends WrappingDoubleArrayMirror {

    private final double[] values;
    
    public MutableDoubleArrayMirror(VirtualMachineHolograph vm, DoubleArrayMirror immutableMirror) {
        super(vm, immutableMirror);
        this.values = new double[immutableMirror.length()];
        for (int i = 0; i < values.length; i++) {
            values[i] = immutableMirror.getDouble(i);
        }
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