package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.IntArrayMirror;
import edu.ubc.mirrors.wrapping.WrappingIntArrayMirror;

public class MutableIntArrayMirror extends WrappingIntArrayMirror {

    private final int[] values;
    
    public MutableIntArrayMirror(VirtualMachineHolograph vm, IntArrayMirror immutableMirror) {
        super(vm, immutableMirror);
        this.values = new int[immutableMirror.length()];
        for (int i = 0; i < values.length; i++) {
            values[i] = immutableMirror.getInt(i);
        }
    }

    @Override
    public int getInt(int index) throws ArrayIndexOutOfBoundsException {
        return values[index];
    }

    @Override
    public void setInt(int index, int i) throws ArrayIndexOutOfBoundsException {
        values[index] = i;
    }
}
