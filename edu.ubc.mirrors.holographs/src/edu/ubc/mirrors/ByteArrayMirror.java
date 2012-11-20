package edu.ubc.mirrors;

public interface ByteArrayMirror extends ArrayMirror {

    public byte getByte(int index) throws ArrayIndexOutOfBoundsException;
    
    public void setByte(int index, byte b) throws ArrayIndexOutOfBoundsException;
}
