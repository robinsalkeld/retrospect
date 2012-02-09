package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.DoubleArrayMirror;
import edu.ubc.mirrors.ClassMirror;

public class NativeDoubleArrayMirror implements DoubleArrayMirror {

    private final double[] array;
    
    public NativeDoubleArrayMirror(double[] array) {
        this.array = array;
    }
    
    public int length() {
        return array.length;
    }

    public ClassMirror<?> getClassMirror() {
        return new NativeClassMirror<Object>(array.getClass());
    }

    public double getDouble(int index) throws ArrayIndexOutOfBoundsException {
        return array[index];
    }

    public void setDouble(int index, double d) throws ArrayIndexOutOfBoundsException {
        array[index] = d;
    }

}
