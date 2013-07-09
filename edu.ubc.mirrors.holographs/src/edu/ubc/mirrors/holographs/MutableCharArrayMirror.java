package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.wrapping.WrappingCharArrayMirror;

public class MutableCharArrayMirror extends WrappingCharArrayMirror {

    private final CharArrayMirror immutableMirror;
    private char[] values;
    
    public MutableCharArrayMirror(VirtualMachineHolograph vm, CharArrayMirror immutableMirror) {
        super(vm, immutableMirror);
        this.immutableMirror = immutableMirror;
        
    }
    
    @Override
    public char getChar(int index) throws ArrayIndexOutOfBoundsException {
        return values != null ? values[index] : immutableMirror.getChar(index);
    }

    @Override
    public void setChar(int index, char c) throws ArrayIndexOutOfBoundsException {
        if (values == null) {
            this.values = new char[immutableMirror.length()];
            for (int i = 0; i < values.length; i++) {
                values[i] = immutableMirror.getChar(i);
            }
        }
        values[index] = c;
    }
}
