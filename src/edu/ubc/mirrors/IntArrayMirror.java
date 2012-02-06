package edu.ubc.mirrors;

public interface IntArrayMirror extends ArrayMirror {

    public int getInt(int index) throws ArrayIndexOutOfBoundsException;
    
    public void setInt(int index, int b) throws ArrayIndexOutOfBoundsException;
}
