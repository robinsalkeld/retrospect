package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.InstanceMirror;

public class WrappingInstanceMirror extends WrappingMirror implements InstanceMirror {

    public WrappingInstanceMirror(WrappingVirtualMachine vm, InstanceMirror wrappedInstance) {
        super(vm, wrappedInstance);
    }

}
