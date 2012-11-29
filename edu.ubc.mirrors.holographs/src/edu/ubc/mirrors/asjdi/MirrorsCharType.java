package edu.ubc.mirrors.asjdi;

import com.sun.jdi.CharType;

import edu.ubc.mirrors.ClassMirror;

public class MirrorsCharType extends MirrorsType implements CharType {

    public MirrorsCharType(MirrorsVirtualMachine vm, ClassMirror wrapped) {
        super(vm, wrapped);
    }

}
