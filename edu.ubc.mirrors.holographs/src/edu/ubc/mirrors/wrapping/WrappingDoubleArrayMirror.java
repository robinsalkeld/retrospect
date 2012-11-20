package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.DoubleArrayMirror;

public class WrappingDoubleArrayMirror extends WrappingMirror implements DoubleArrayMirror {

    private final DoubleArrayMirror wrappedArray;
    
    public WrappingDoubleArrayMirror(WrappingVirtualMachine vm, DoubleArrayMirror wrappedArray) {
        super(vm, wrappedArray);
        this.wrappedArray = wrappedArray;
    }

    @Override
    public int length() {
        return wrappedArray.length();
    }

    @Override
    public double getDouble(int index) throws ArrayIndexOutOfBoundsException {
        return wrappedArray.getDouble(index);
    }

    @Override
    public void setDouble(int index, double l) throws ArrayIndexOutOfBoundsException {
        wrappedArray.setDouble(index, l);
    }
}
