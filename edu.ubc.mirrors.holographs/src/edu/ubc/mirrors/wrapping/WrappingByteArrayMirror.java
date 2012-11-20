package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ByteArrayMirror;

public class WrappingByteArrayMirror extends WrappingMirror implements ByteArrayMirror {

    private final ByteArrayMirror wrappedArray;
    
    public WrappingByteArrayMirror(WrappingVirtualMachine vm, ByteArrayMirror wrappedArray) {
        super(vm, wrappedArray);
        this.wrappedArray = wrappedArray;
    }

    @Override
    public int length() {
        return wrappedArray.length();
    }

    @Override
    public byte getByte(int index) throws ArrayIndexOutOfBoundsException {
        return wrappedArray.getByte(index);
    }

    @Override
    public void setByte(int index, byte b) throws ArrayIndexOutOfBoundsException {
        wrappedArray.setByte(index, b);
    }
}
