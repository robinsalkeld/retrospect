package edu.ubc.mirrors;

public interface ShortArrayMirror extends ArrayMirror {

    public short getShort(int index) throws ArrayIndexOutOfBoundsException;
    
    public void setShort(int index, short b) throws ArrayIndexOutOfBoundsException;
}
