package edu.ubc.mirrors.asjdi;

import com.sun.jdi.VoidType;

import edu.ubc.mirrors.ClassMirror;

public class MirrorsVoidType extends MirrorsType implements VoidType {

    public MirrorsVoidType(MirrorsVirtualMachine vm, ClassMirror wrapped) {
        super(vm, wrapped);
    }

}
