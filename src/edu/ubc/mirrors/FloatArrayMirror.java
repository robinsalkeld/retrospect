package edu.ubc.mirrors;

public interface FloatArrayMirror extends ArrayMirror {

    public float get(int index) throws ArrayIndexOutOfBoundsException;
    
    public void set(int index, float b) throws ArrayIndexOutOfBoundsException;
}
