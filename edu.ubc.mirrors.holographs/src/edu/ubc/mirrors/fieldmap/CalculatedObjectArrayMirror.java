package edu.ubc.mirrors.fieldmap;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;

public abstract class CalculatedObjectArrayMirror implements ObjectArrayMirror {

    private final ClassMirror klass;
    private final int length;
    
    public CalculatedObjectArrayMirror(ClassMirror klass, int length) {
        this.klass = klass;
        this.length = length;
    }
    
    @Override
    public ClassMirror getClassMirror() {
        return klass;
    }

    @Override
    public int identityHashCode() {
        return hashCode();
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public abstract ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException;

    @Override
    public void set(int index, ObjectMirror o) throws ArrayIndexOutOfBoundsException {
        throw new InternalError();
    }
}
