package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.ShortArrayMirror;
import edu.ubc.mirrors.wrapping.WrappingShortArrayMirror;

public class MutableShortArrayMirror extends WrappingShortArrayMirror {

    private final short[] values;
    
    public MutableShortArrayMirror(VirtualMachineHolograph vm, ShortArrayMirror immutableMirror) {
        super(vm, immutableMirror);
        this.values = new short[immutableMirror.length()];
        for (int i = 0; i < values.length; i++) {
            values[i] = immutableMirror.getShort(i);
        }
    }
    
    @Override
    public short getShort(int index) throws ArrayIndexOutOfBoundsException {
        return values[index];
    }

    @Override
    public void setShort(int index, short s) throws ArrayIndexOutOfBoundsException {
        values[index] = s;
    }
}
