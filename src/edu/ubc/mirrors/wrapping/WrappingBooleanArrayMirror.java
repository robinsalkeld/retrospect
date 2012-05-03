package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.BooleanArrayMirror;

public class WrappingBooleanArrayMirror extends WrappingMirror implements BooleanArrayMirror {

    private final BooleanArrayMirror wrappedArray;
    
    public WrappingBooleanArrayMirror(WrappingVirtualMachine vm, BooleanArrayMirror wrappedArray) {
        super(vm, wrappedArray);
        this.wrappedArray = wrappedArray;
    }

    @Override
    public int length() {
        return wrappedArray.length();
    }

    @Override
    public boolean getBoolean(int index) throws ArrayIndexOutOfBoundsException {
        return wrappedArray.getBoolean(index);
    }

    @Override
    public void setBoolean(int index, boolean b) throws ArrayIndexOutOfBoundsException {
        wrappedArray.setBoolean(index, b);
    }
}
