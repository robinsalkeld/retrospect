package edu.ubc.mirrors;

public interface ByteArrayMirror extends ArrayMirror {

    public byte get(int index) throws ArrayIndexOutOfBoundsException;
    
    public void set(int index, byte b) throws ArrayIndexOutOfBoundsException;
}
