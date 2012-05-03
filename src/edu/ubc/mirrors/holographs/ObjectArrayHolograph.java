package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.wrapping.WrappingObjectArrayMirror;

public class ObjectArrayHolograph extends WrappingObjectArrayMirror {

    public ObjectArrayHolograph(VirtualMachineHolograph vm, ObjectArrayMirror wrappedArray) {
        super(vm, wrappedArray);
    }
}
