package edu.ubc.mirrors.mutable;

import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.wrapping.WrappingObjectArrayMirror;

public class MutableObjectArrayMirror extends WrappingObjectArrayMirror {

    private final ObjectMirror[] mutableValues;
    private final ObjectArrayMirror immutableMirror;
    
    public MutableObjectArrayMirror(MutableVirtualMachineMirror vm, ObjectArrayMirror immutableMirror) {
        super(vm, immutableMirror);
        this.immutableMirror = immutableMirror;
        this.mutableValues = new ObjectMirror[immutableMirror.length()];
        
        for (int index = 0; index < mutableValues.length; index++) {
            mutableValues[index] = vm.getWrappedMirror(immutableMirror.get(index));
        }
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
