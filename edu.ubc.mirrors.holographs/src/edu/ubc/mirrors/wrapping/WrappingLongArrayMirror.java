package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.LongArrayMirror;

public class WrappingLongArrayMirror extends WrappingMirror implements LongArrayMirror {

    private final LongArrayMirror wrappedArray;
    
    public WrappingLongArrayMirror(WrappingVirtualMachine vm, LongArrayMirror wrappedArray) {
        super(vm, wrappedArray);
        this.wrappedArray = wrappedArray;
    }

    @Override
    public int length() {
        return wrappedArray.length();
    }

    @Override
    public long getLong(int index) throws ArrayIndexOutOfBoundsException {
        return wrappedArray.getLong(index);
    }

    @Override
    public void setLong(int index, long l) throws ArrayIndexOutOfBoundsException {
        wrappedArray.setLong(index, l);
    }
}
