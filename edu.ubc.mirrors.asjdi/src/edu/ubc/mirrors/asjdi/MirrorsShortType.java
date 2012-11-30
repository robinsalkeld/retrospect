package edu.ubc.mirrors.asjdi;

import com.sun.jdi.ShortType;

import edu.ubc.mirrors.ClassMirror;

public class MirrorsShortType extends MirrorsType implements ShortType {

    public MirrorsShortType(MirrorsVirtualMachine vm, ClassMirror wrapped) {
        super(vm, wrapped);
    }

}
