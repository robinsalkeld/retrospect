package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.LongArrayMirror;
import edu.ubc.mirrors.wrapping.WrappingLongArrayMirror;

public class MutableLongArrayMirror extends WrappingLongArrayMirror {

    private final long[] values;
    
    public MutableLongArrayMirror(VirtualMachineHolograph vm, LongArrayMirror immutableMirror) {
        super(vm, immutableMirror);
        this.values = new long[immutableMirror.length()];
        for (int i = 0; i < values.length; i++) {
            values[i] = immutableMirror.getLong(i);
        }
    }
    
    @Override
    public long getLong(int index) throws ArrayIndexOutOfBoundsException {
        return values[index];
    }

    @Override
    public void setLong(int index, long i) throws ArrayIndexOutOfBoundsException {
        values[index] = i;
    }
}