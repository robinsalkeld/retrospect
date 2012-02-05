package edu.ubc.mirrors;

public interface ObjectArrayMirror extends ArrayMirror {
    
    public Object get(int index) throws ArrayIndexOutOfBoundsException;
    
    public void set(int index, Object o) throws ArrayIndexOutOfBoundsException;
}
