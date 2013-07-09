package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.BooleanArrayMirror;
import edu.ubc.mirrors.wrapping.WrappingBooleanArrayMirror;

public class MutableBooleanArrayMirror extends WrappingBooleanArrayMirror {

    private final BooleanArrayMirror immutableMirror;
    private boolean[] values;
    
    public MutableBooleanArrayMirror(VirtualMachineHolograph vm, BooleanArrayMirror immutableMirror) {
        super(vm, immutableMirror);
        this.immutableMirror = immutableMirror;
        
    }
    
    @Override
    public boolean getBoolean(int index) throws ArrayIndexOutOfBoundsException {
        return values != null ? values[index] : immutableMirror.getBoolean(index);
    }

    @Override
    public void setBoolean(int index, boolean b) throws ArrayIndexOutOfBoundsException {
        if (values == null) {
            this.values = new boolean[immutableMirror.length()];
            for (int i = 0; i < values.length; i++) {
                values[i] = immutableMirror.getBoolean(i);
            }
        }
        values[index] = b;
    }
}
