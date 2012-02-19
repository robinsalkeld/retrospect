package edu.ubc.mirrors.fieldmap;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;

import edu.ubc.mirrors.BoxingArrayMirror;

public class DirectArrayMirror extends BoxingArrayMirror implements ObjectArrayMirror {

    private final ClassMirror classMirror;
    private final Object[] array;
    
    public DirectArrayMirror(ClassMirror classMirror, int length) {
        this.classMirror = classMirror;
        this.array = new Object[length];
    }
    
    public int length() {
        return array.length;
    }

    public ClassMirror getClassMirror() {
        return classMirror;
    }

    public ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException {
        return (ObjectMirror)array[index];
    }

    public void set(int index, ObjectMirror o) throws ArrayIndexOutOfBoundsException {
        array[index] = o;
    }

    @Override
    protected Object getBoxedValue(int index) throws ArrayIndexOutOfBoundsException {
        return array[index];
    }

    @Override
    protected void setBoxedValue(int index, Object o) throws ArrayIndexOutOfBoundsException {
        array[index] = o;
    }
}
