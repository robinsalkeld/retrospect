package edu.ubc.mirrors.mutable;

import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.ClassMirror;

public class MutableCharArrayMirror implements CharArrayMirror {

    private final CharArrayMirror immutableMirror;
    private final char[] values;
    
    public MutableCharArrayMirror(CharArrayMirror immutableMirror) {
        this.immutableMirror = immutableMirror;
        this.values = new char[immutableMirror.length()];
        for (int i = 0; i < values.length; i++) {
            values[i] = immutableMirror.getChar(i);
        }
    }
    
    @Override
    public ClassMirror getClassMirror() {
        return immutableMirror.getClassMirror();
    }

    @Override
    public int length() {
        return immutableMirror.length();
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
