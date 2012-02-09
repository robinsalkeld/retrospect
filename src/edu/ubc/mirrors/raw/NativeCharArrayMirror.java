package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.ClassMirror;

public class NativeCharArrayMirror implements CharArrayMirror {

    private final char[] array;
    
    public NativeCharArrayMirror(char[] array) {
        this.array = array;
    }
    
    public int length() {
        return array.length;
    }

    public ClassMirror<?> getClassMirror() {
        return new NativeClassMirror<Object>(array.getClass());
    }

    public char getChar(int index) throws ArrayIndexOutOfBoundsException {
        return array[index];
    }

    public void setChar(int index, char c) throws ArrayIndexOutOfBoundsException {
        array[index] = c;
    }

}
