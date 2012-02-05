package edu.ubc.mirrors;

public interface CharArrayMirror extends ArrayMirror {

    public char get(int index) throws ArrayIndexOutOfBoundsException;
    
    public void set(int index, char b) throws ArrayIndexOutOfBoundsException;
}
