package edu.ubc.mirrors.asjdi;

import com.sun.jdi.VoidType;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.asjdi.MirrorsType;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsVoidType extends MirrorsType implements VoidType {

    public MirrorsVoidType(MirrorsVirtualMachine vm, ClassMirror wrapped) {
        super(vm, wrapped);
    }

}
