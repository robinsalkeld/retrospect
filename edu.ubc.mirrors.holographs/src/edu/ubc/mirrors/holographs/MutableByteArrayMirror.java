package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.wrapping.WrappingByteArrayMirror;

public class MutableByteArrayMirror extends WrappingByteArrayMirror {

    private final ByteArrayMirror immutableMirror;
    private byte[] values;
    
    public MutableByteArrayMirror(VirtualMachineHolograph vm, ByteArrayMirror immutableMirror) {
        super(vm, immutableMirror);
        this.immutableMirror = immutableMirror;
    }
    
    @Override
    public byte getByte(int index) throws ArrayIndexOutOfBoundsException {
        return values != null ? values[index] : immutableMirror.getByte(index);
    }

    @Override
    public void setByte(int index, byte b) throws ArrayIndexOutOfBoundsException {
        if (values == null) {
            this.values = new byte[immutableMirror.length()];
            for (int i = 0; i < values.length; i++) {
                values[i] = immutableMirror.getByte(i);
            }
        }
        values[index] = b;
    }
}
