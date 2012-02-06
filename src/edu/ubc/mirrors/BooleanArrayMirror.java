package edu.ubc.mirrors;

public interface BooleanArrayMirror extends ArrayMirror {

    public boolean getBoolean(int index) throws ArrayIndexOutOfBoundsException;
    
    public void setBoolean(int index, boolean b) throws ArrayIndexOutOfBoundsException;
}
