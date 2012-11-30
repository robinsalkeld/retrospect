package edu.ubc.mirrors.asjdi;

import com.sun.jdi.IntegerType;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.asjdi.MirrorsType;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsIntegerType extends MirrorsType implements IntegerType {

    public MirrorsIntegerType(MirrorsVirtualMachine vm, ClassMirror wrapped) {
        super(vm, wrapped);
    }

}
