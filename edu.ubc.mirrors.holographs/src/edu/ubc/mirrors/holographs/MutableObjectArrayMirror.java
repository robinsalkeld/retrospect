package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.wrapping.WrappingObjectArrayMirror;

public class MutableObjectArrayMirror extends WrappingObjectArrayMirror {

    private ObjectMirror[] mutableValues;
    private final ObjectArrayMirror immutableMirror;
    
    public MutableObjectArrayMirror(VirtualMachineHolograph vm, ObjectArrayMirror immutableMirror) {
        super(vm, immutableMirror);
        this.immutableMirror = immutableMirror;
        
    }
    
    @Override
    public ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException {
        return mutableValues != null ? mutableValues[index] : vm.getWrappedMirror(immutableMirror.get(index));
    }

    @Override
    public void set(int i, ObjectMirror o) throws ArrayIndexOutOfBoundsException {
        if (mutableValues == null) {
            this.mutableValues = new ObjectMirror[immutableMirror.length()];
            for (int index = 0; index < mutableValues.length; index++) {
                mutableValues[index] = vm.getWrappedMirror(immutableMirror.get(index));
            }
        }
        mutableValues[i] = o;
    }
    
    @Override
    public String toString() {
        return "MutableObjectArrayMirror on " + immutableMirror;
    }

}
