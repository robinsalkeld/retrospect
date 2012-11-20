package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;

public class WrappingObjectArrayMirror extends WrappingMirror implements ObjectArrayMirror {

    private final ObjectArrayMirror wrappedArray;
    
    public WrappingObjectArrayMirror(WrappingVirtualMachine vm, ObjectArrayMirror wrappedArray) {
        super(vm, wrappedArray);
        this.wrappedArray = wrappedArray;
    }

    @Override
    public int length() {
        return wrappedArray.length();
    }

    @Override
    public ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException {
        return vm.getWrappedMirror(wrappedArray.get(index));
    }

    @Override
    public void set(int index, ObjectMirror o) throws ArrayIndexOutOfBoundsException {
        wrappedArray.set(index, vm.unwrapMirror(o));
    }
}
