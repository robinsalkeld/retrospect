package edu.ubc.mirrors.asjdi;

import com.sun.jdi.LongType;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.asjdi.MirrorsType;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsLongType extends MirrorsType implements LongType {

    public MirrorsLongType(MirrorsVirtualMachine vm, ClassMirror wrapped) {
        super(vm, wrapped);
    }

}
