package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.wrapping.WrappingCharArrayMirror;

public class MutableCharArrayMirror extends WrappingCharArrayMirror {

    private final char[] values;
    
    public MutableCharArrayMirror(VirtualMachineHolograph vm, CharArrayMirror immutableMirror) {
        super(vm, immutableMirror);
        this.values = new char[immutableMirror.length()];
        for (int i = 0; i < values.length; i++) {
            values[i] = immutableMirror.getChar(i);
        }
    }
    
    @Override
    public char getChar(int index) throws ArrayIndexOutOfBoundsException {
        return values[index];
    }

    @Override
    public void setChar(int index, char c) throws ArrayIndexOutOfBoundsException {
        values[index] = c;
    }
}