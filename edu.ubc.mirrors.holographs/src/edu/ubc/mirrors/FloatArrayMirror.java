package edu.ubc.mirrors;

public interface FloatArrayMirror extends ArrayMirror {

    public float getFloat(int index) throws ArrayIndexOutOfBoundsException;
    
    public void setFloat(int index, float b) throws ArrayIndexOutOfBoundsException;
}
