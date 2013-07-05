package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.BooleanArrayMirror;
import edu.ubc.mirrors.ClassMirror;

public class NativeBooleanArrayMirror extends NativeObjectMirror implements BooleanArrayMirror {

    private final boolean[] array;
    
    public NativeBooleanArrayMirror(boolean[] array) {
        super(array);
        this.array = array;
    }
    
    public int length() {
        return array.length;
    }

    public boolean getBoolean(int index) throws ArrayIndexOutOfBoundsException {
        return array[index];
    }

    public void setBoolean(int index, boolean b) throws ArrayIndexOutOfBoundsException {
        array[index] = b;
    }

    @Override
    public byte getByte(int index) throws ArrayIndexOutOfBoundsException {
        return getBoolean(index) ? (byte)1 : 0;
    }
    
    public void setByte(int index, byte b) throws ArrayIndexOutOfBoundsException {
        setBoolean(index, b != 0);
    };
}