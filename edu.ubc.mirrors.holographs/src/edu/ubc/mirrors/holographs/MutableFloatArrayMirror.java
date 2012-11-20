package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.FloatArrayMirror;
import edu.ubc.mirrors.wrapping.WrappingFloatArrayMirror;

public class MutableFloatArrayMirror extends WrappingFloatArrayMirror {

    private final float[] values;
    
    public MutableFloatArrayMirror(VirtualMachineHolograph vm, FloatArrayMirror immutableMirror) {
        super(vm, immutableMirror);
        this.values = new float[immutableMirror.length()];
        for (int i = 0; i < values.length; i++) {
            values[i] = immutableMirror.getFloat(i);
        }
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
