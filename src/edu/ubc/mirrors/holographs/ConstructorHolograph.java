package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.wrapping.WrappingConstructorMirror;
import edu.ubc.mirrors.wrapping.WrappingVirtualMachine;

public class ConstructorHolograph extends WrappingConstructorMirror {

    public ConstructorHolograph(WrappingVirtualMachine vm, ConstructorMirror wrapped) {
        super(vm, wrapped);
    }
}
