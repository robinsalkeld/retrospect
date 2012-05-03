package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ShortArrayMirror;

public class WrappingShortArrayMirror extends WrappingMirror implements ShortArrayMirror {

    private final ShortArrayMirror wrappedArray;
    
    public WrappingShortArrayMirror(WrappingVirtualMachine vm, ShortArrayMirror wrappedArray) {
        super(vm, wrappedArray);
        this.wrappedArray = wrappedArray;
    }

    @Override
    public int length() {
        return wrappedArray.length();
    }

    @Override
    public short getShort(int index) throws ArrayIndexOutOfBoundsException {
        return wrappedArray.getShort(index);
    }

    @Override
    public void setShort(int index, short s) throws ArrayIndexOutOfBoundsException {
        wrappedArray.setShort(index, s);
    }
}
