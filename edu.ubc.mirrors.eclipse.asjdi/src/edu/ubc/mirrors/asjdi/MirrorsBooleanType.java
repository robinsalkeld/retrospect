package edu.ubc.mirrors.asjdi;

import com.sun.jdi.BooleanType;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.asjdi.MirrorsType;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsBooleanType extends MirrorsType implements BooleanType {

    public MirrorsBooleanType(MirrorsVirtualMachine vm, ClassMirror wrapped) {
        super(vm, wrapped);
    }

}
