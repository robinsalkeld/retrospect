package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.LongArrayMirror;
import edu.ubc.mirrors.wrapping.WrappingLongArrayMirror;

public class MutableLongArrayMirror extends WrappingLongArrayMirror {

    private final LongArrayMirror immutableMirror;
    private long[] values;
    
    public MutableLongArrayMirror(VirtualMachineHolograph vm, LongArrayMirror immutableMirror) {
        super(vm, immutableMirror);
        this.immutableMirror = immutableMirror;
    }
    
    @Override
    public long getLong(int index) throws ArrayIndexOutOfBoundsException {
         return values != null ? values[index] : immutableMirror.getLong(index);
    }

    @Override
    public void setLong(int index, long l) throws ArrayIndexOutOfBoundsException {
        if (values == null) {
            this.values = new long[immutableMirror.length()];
            for (int i = 0; i < values.length; i++) {
                values[i] = immutableMirror.getLong(i);
            }
        }
        values[index] = l;
    }
}
