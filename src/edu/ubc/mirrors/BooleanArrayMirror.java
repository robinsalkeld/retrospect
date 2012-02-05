package edu.ubc.mirrors;

public interface BooleanArrayMirror extends ArrayMirror {

    public boolean get(int index) throws ArrayIndexOutOfBoundsException;
    
    public void set(int index, boolean b) throws ArrayIndexOutOfBoundsException;
}
