package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.FloatArrayMirror;
import edu.ubc.mirrors.wrapping.WrappingFloatArrayMirror;

public class MutableFloatArrayMirror extends WrappingFloatArrayMirror {

    private final FloatArrayMirror immutableMirror;
    private float[] values;
    
    public MutableFloatArrayMirror(VirtualMachineHolograph vm, FloatArrayMirror immutableMirror) {
        super(vm, immutableMirror);
        this.immutableMirror = immutableMirror;
        
    }
    
    @Override
    public float getFloat(int index) throws ArrayIndexOutOfBoundsException {
        return values != null ? values[index] : immutableMirror.getFloat(index);
    }

    @Override
    public void setFloat(int index, float f) throws ArrayIndexOutOfBoundsException {
        if (values == null) {
            this.values = new float[immutableMirror.length()];
            for (int i = 0; i < values.length; i++) {
                values[i] = immutableMirror.getFloat(i);
            }
        }
        values[index] = f;
    }
}
