package edu.ubc.mirrors;

public interface DoubleArrayMirror extends ArrayMirror {

    public double getDouble(int index) throws ArrayIndexOutOfBoundsException;
    
    public void setDouble(int index, double b) throws ArrayIndexOutOfBoundsException;
}
