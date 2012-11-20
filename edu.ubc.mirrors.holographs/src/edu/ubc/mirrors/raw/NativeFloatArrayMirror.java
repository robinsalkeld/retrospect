package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.FloatArrayMirror;
import edu.ubc.mirrors.ClassMirror;

public class NativeFloatArrayMirror extends NativeObjectMirror implements FloatArrayMirror {

    private final float[] array;
    
    public NativeFloatArrayMirror(float[] array) {
        super(array);
        this.array = array;
    }
    
    public int length() {
        return array.length;
    }

    public ClassMirror getClassMirror() {
        return new NativeClassMirror(array.getClass());
    }

    public float getFloat(int index) throws ArrayIndexOutOfBoundsException {
        return array[index];
    }

    public void setFloat(int index, float f) throws ArrayIndexOutOfBoundsException {
        array[index] = f;
    }

}
