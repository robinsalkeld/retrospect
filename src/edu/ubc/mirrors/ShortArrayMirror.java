package edu.ubc.mirrors;

public interface ShortArrayMirror extends ArrayMirror {

    public short get(int index) throws ArrayIndexOutOfBoundsException;
    
    public void set(int index, short b) throws ArrayIndexOutOfBoundsException;
}
