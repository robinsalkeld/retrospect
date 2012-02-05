package edu.ubc.mirrors;

public interface LongArrayMirror extends ArrayMirror {

    public long get(int index) throws ArrayIndexOutOfBoundsException;
    
    public void set(int index, long b) throws ArrayIndexOutOfBoundsException;
}
