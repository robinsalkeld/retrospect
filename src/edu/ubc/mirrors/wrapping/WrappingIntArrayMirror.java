package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.IntArrayMirror;

public class WrappingIntArrayMirror extends WrappingMirror implements IntArrayMirror {

    private final IntArrayMirror wrappedArray;
    
    public WrappingIntArrayMirror(WrappingVirtualMachine vm, IntArrayMirror wrappedArray) {
        super(vm, wrappedArray);
        this.wrappedArray = wrappedArray;
    }

    @Override
    public int length() {
        return wrappedArray.length();
    }

    @Override
    public int getInt(int index) throws ArrayIndexOutOfBoundsException {
        return wrappedArray.getInt(index);
    }

    @Override
    public void setInt(int index, int i) throws ArrayIndexOutOfBoundsException {
        wrappedArray.setInt(index, i);
    }
}
