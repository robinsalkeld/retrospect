package edu.ubc.mirrors;

public interface DoubleArrayMirror extends ArrayMirror {

    public double get(int index) throws ArrayIndexOutOfBoundsException;
    
    public void set(int index, double b) throws ArrayIndexOutOfBoundsException;
}
