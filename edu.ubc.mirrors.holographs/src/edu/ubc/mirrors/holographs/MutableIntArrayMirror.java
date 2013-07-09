package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.IntArrayMirror;
import edu.ubc.mirrors.wrapping.WrappingIntArrayMirror;

public class MutableIntArrayMirror extends WrappingIntArrayMirror {

    private final IntArrayMirror immutableMirror;
    private int[] values;
    
    public MutableIntArrayMirror(VirtualMachineHolograph vm, IntArrayMirror immutableMirror) {
        super(vm, immutableMirror);
        this.immutableMirror = immutableMirror;
        
    }

    @Override
    public int getInt(int index) throws ArrayIndexOutOfBoundsException {
         return values != null ? values[index] : immutableMirror.getInt(index);
    }

    @Override
    public void setInt(int index, int value) throws ArrayIndexOutOfBoundsException {
        if (values == null) {
            this.values = new int[immutableMirror.length()];
            for (int i = 0; i < values.length; i++) {
                values[i] = immutableMirror.getInt(i);
            }
        }
        values[index] = value;
    }
}
