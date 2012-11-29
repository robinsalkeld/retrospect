package edu.ubc.mirrors.asjdi;

import com.sun.jdi.DoubleType;

import edu.ubc.mirrors.ClassMirror;

public class MirrorsDoubleType extends MirrorsType implements DoubleType {

    public MirrorsDoubleType(MirrorsVirtualMachine vm, ClassMirror wrapped) {
        super(vm, wrapped);
    }

}
