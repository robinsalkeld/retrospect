package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.BooleanArrayMirror;
import edu.ubc.mirrors.ClassMirror;

public class NativeBooleanArrayMirror implements BooleanArrayMirror {

    private final boolean[] array;
    
    public NativeBooleanArrayMirror(boolean[] array) {
        this.array = array;
    }
    
    public int length() {
        return array.length;
    }

    public ClassMirror getClassMirror() {
        return new NativeClassMirror(array.getClass());
    }

    public boolean getBoolean(int index) throws ArrayIndexOutOfBoundsException {
        return array[index];
    }

    public void setBoolean(int index, boolean b) throws ArrayIndexOutOfBoundsException {
        array[index] = b;
    }

}
