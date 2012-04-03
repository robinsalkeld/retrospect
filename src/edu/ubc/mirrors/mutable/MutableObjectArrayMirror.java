package edu.ubc.mirrors.mutable;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;

public class MutableObjectArrayMirror implements ObjectArrayMirror {

    private final ObjectMirror[] mutableValues;
    private final ObjectArrayMirror immutableMirror;
    
    public MutableObjectArrayMirror(MutableClassMirrorLoader loader, ObjectArrayMirror immutableMirror) {
        this.immutableMirror = immutableMirror;
        this.mutableValues = new ObjectMirror[immutableMirror.length()];
        
        for (int index = 0; index < mutableValues.length; index++) {
            mutableValues[index] = loader.makeMirror(immutableMirror.get(index));
        }
    }
    
    @Override
    public int length() {
        return immutableMirror.length();
    }

    @Override
    public ClassMirror getClassMirror() {
        return immutableMirror.getClassMirror();
    }

    @Override
    public ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException {
        return mutableValues[index];
    }

    @Override
    public void set(int index, ObjectMirror o) throws ArrayIndexOutOfBoundsException {
        mutableValues[index] = o;
    }
    
    @Override
    public String toString() {
        return "MutableObjectArrayMirror on " + immutableMirror;
    }

}
