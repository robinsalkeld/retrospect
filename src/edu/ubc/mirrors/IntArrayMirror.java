package edu.ubc.mirrors;

public interface IntArrayMirror extends ArrayMirror {

    public int get(int index) throws ArrayIndexOutOfBoundsException;
    
    public void set(int index, int b) throws ArrayIndexOutOfBoundsException;
}
