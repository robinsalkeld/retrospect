package edu.ubc.mirrors;

public interface LongArrayMirror extends ArrayMirror {

    public long getLong(int index) throws ArrayIndexOutOfBoundsException;
    
    public void setLong(int index, long b) throws ArrayIndexOutOfBoundsException;
}
