package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.ClassMirror;

public class NativeCharArrayMirror extends NativeObjectMirror implements CharArrayMirror {

    private final char[] array;
    
    public NativeCharArrayMirror(char[] array) {
        super(array);
        this.array = array;
    }
    
    public int length() {
        return array.length;
    }

    public char getChar(int index) throws ArrayIndexOutOfBoundsException {
        return array[index];
    }

    public void setChar(int index, char c) throws ArrayIndexOutOfBoundsException {
        array[index] = c;
    }

}
