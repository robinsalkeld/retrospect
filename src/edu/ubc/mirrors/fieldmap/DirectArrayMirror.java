package edu.ubc.mirrors.fieldmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    
    public DirectArrayMirror(ClassMirror classMirror, int[] dims) {
        this(classMirror, dimsList(dims));
    }
    
    private static List<Integer> dimsList(int[] dims) {
        List<Integer> result = new ArrayList<Integer>(dims.length);
        for (int dim : dims) {
            result.add(dim);
        }
        return result;
    }
    
    private DirectArrayMirror(ClassMirror classMirror, List<Integer> dims) {
        this.classMirror = classMirror;
        this.array = new Object[dims.get(0)];
        
        if (dims.size() > 1) {
            ClassMirror componentClassMirror = classMirror.getComponentClassMirror();
            List<Integer> componentDims = dims.subList(1, dims.size());
            for (int i = 0; i < array.length; i++) {
                this.array[i] = new DirectArrayMirror(componentClassMirror, componentDims);
            }
        }
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
