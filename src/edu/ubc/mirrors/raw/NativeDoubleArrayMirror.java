package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.DoubleArrayMirror;
import edu.ubc.mirrors.ClassMirror;

public class NativeDoubleArrayMirror extends NativeObjectMirror implements DoubleArrayMirror {

    private final double[] array;
    
    public NativeDoubleArrayMirror(double[] array) {
        super(array);
        this.array = array;
    }
    
    public int length() {
        return array.length;
    }

    public ClassMirror getClassMirror() {
        return new NativeClassMirror(array.getClass());
    }

    public double getDouble(int index) throws ArrayIndexOutOfBoundsException {
        return array[index];
    }

    public void setDouble(int index, double d) throws ArrayIndexOutOfBoundsException {
        array[index] = d;
    }

}
