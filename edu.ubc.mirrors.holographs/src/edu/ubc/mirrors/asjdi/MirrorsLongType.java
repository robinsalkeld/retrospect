package edu.ubc.mirrors.asjdi;

import com.sun.jdi.LongType;

import edu.ubc.mirrors.ClassMirror;

public class MirrorsLongType extends MirrorsType implements LongType {

    public MirrorsLongType(MirrorsVirtualMachine vm, ClassMirror wrapped) {
        super(vm, wrapped);
    }

}
