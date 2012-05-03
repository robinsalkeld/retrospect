package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.FloatArrayMirror;

public class WrappingFloatArrayMirror extends WrappingMirror implements FloatArrayMirror {

    private final FloatArrayMirror wrappedArray;
    
    public WrappingFloatArrayMirror(WrappingVirtualMachine vm, FloatArrayMirror wrappedArray) {
        super(vm, wrappedArray);
        this.wrappedArray = wrappedArray;
    }

    @Override
    public int length() {
        return wrappedArray.length();
    }

    @Override
    public float getFloat(int index) throws ArrayIndexOutOfBoundsException {
        return wrappedArray.getFloat(index);
    }

    @Override
    public void setFloat(int index, float l) throws ArrayIndexOutOfBoundsException {
        wrappedArray.setFloat(index, l);
    }
}
