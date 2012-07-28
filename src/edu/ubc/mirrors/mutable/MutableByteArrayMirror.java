package edu.ubc.mirrors.mutable;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.wrapping.WrappingBooleanArrayMirror;
import edu.ubc.mirrors.wrapping.WrappingByteArrayMirror;

public class MutableByteArrayMirror extends WrappingByteArrayMirror {

    private final byte[] values;
    
    public MutableByteArrayMirror(MutableVirtualMachineMirror vm, ByteArrayMirror immutableMirror) {
        super(vm, immutableMirror);
        this.values = new byte[immutableMirror.length()];
        for (int i = 0; i < values.length; i++) {
            values[i] = immutableMirror.getByte(i);
        }
    }
    
    @Override
    public byte getByte(int index) throws ArrayIndexOutOfBoundsException {
        return values[index];
    }

    @Override
    public void setByte(int index, byte b) throws ArrayIndexOutOfBoundsException {
        values[index] = b;
    }
}
