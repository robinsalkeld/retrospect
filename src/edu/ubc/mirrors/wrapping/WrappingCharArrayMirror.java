package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.CharArrayMirror;

public class WrappingCharArrayMirror extends WrappingMirror implements CharArrayMirror {

    private final CharArrayMirror wrappedArray;
    
    public WrappingCharArrayMirror(WrappingVirtualMachine vm, CharArrayMirror wrappedArray) {
        super(vm, wrappedArray);
        this.wrappedArray = wrappedArray;
    }

    @Override
    public int length() {
        return wrappedArray.length();
    }

    @Override
    public char getChar(int index) throws ArrayIndexOutOfBoundsException {
        return wrappedArray.getChar(index);
    }

    @Override
    public void setChar(int index, char c) throws ArrayIndexOutOfBoundsException {
        wrappedArray.setChar(index, c);
    }
}
