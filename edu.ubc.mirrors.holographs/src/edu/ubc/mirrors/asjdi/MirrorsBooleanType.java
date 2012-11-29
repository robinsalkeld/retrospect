package edu.ubc.mirrors.asjdi;

import com.sun.jdi.BooleanType;

import edu.ubc.mirrors.ClassMirror;

public class MirrorsBooleanType extends MirrorsType implements BooleanType {

    public MirrorsBooleanType(MirrorsVirtualMachine vm, ClassMirror wrapped) {
        super(vm, wrapped);
    }

}
