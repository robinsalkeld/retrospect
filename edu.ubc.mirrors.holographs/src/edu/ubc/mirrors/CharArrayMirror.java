package edu.ubc.mirrors;

public interface CharArrayMirror extends ArrayMirror {

    public char getChar(int index) throws ArrayIndexOutOfBoundsException;
    
    public void setChar(int index, char b) throws ArrayIndexOutOfBoundsException;
}
