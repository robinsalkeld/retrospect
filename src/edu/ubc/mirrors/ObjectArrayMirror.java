package edu.ubc.mirrors;

public interface ObjectArrayMirror extends ArrayMirror {
    
    public ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException;
    
    public void set(int index, ObjectMirror o) throws ArrayIndexOutOfBoundsException;
}
