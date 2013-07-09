package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.DoubleArrayMirror;
import edu.ubc.mirrors.wrapping.WrappingDoubleArrayMirror;

public class MutableDoubleArrayMirror extends WrappingDoubleArrayMirror {

    private final DoubleArrayMirror immutableMirror;
    private double[] values;
    
    public MutableDoubleArrayMirror(VirtualMachineHolograph vm, DoubleArrayMirror immutableMirror) {
        super(vm, immutableMirror);
        this.immutableMirror = immutableMirror;
    }

    @Override
    public double getDouble(int index) throws ArrayIndexOutOfBoundsException {
        return values != null ? values[index] : immutableMirror.getDouble(index);
    }

    @Override
    public void setDouble(int index, double d) throws ArrayIndexOutOfBoundsException {
        if (values == null) {
            this.values = new double[immutableMirror.length()];
            for (int i = 0; i < values.length; i++) {
                values[i] = immutableMirror.getDouble(i);
            }
        }
        values[index] = d;
    }
}
