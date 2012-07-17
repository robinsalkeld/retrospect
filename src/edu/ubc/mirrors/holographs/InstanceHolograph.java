package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.raw.NativeObjectMirror;
import edu.ubc.mirrors.wrapping.WrappingInstanceMirror;

public class InstanceHolograph extends WrappingInstanceMirror {

    public InstanceHolograph(VirtualMachineHolograph vm, InstanceMirror wrappedInstance) {
        super(vm, wrappedInstance);
    }
}
