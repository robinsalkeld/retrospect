package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.ShortArrayMirror;
import edu.ubc.mirrors.wrapping.WrappingShortArrayMirror;

public class MutableShortArrayMirror extends WrappingShortArrayMirror {

    private final ShortArrayMirror immutableMirror;
    private short[] values;
    
    public MutableShortArrayMirror(VirtualMachineHolograph vm, ShortArrayMirror immutableMirror) {
        super(vm, immutableMirror);
        this.immutableMirror = immutableMirror;
    }
    
    @Override
    public short getShort(int index) throws ArrayIndexOutOfBoundsException {
        return values != null ? values[index] : immutableMirror.getShort(index);
    }

    @Override
    public void setShort(int index, short s) throws ArrayIndexOutOfBoundsException {
        if (values == null) {
            this.values = new short[immutableMirror.length()];
            for (int i = 0; i < values.length; i++) {
                values[i] = immutableMirror.getShort(i);
            }
        }
        values[index] = s;
    }
}
