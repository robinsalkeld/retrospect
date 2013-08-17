package edu.ubc.mirrors.asjdi;

import com.sun.jdi.CharType;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.asjdi.MirrorsType;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsCharType extends MirrorsType implements CharType {

    public MirrorsCharType(MirrorsVirtualMachine vm, ClassMirror wrapped) {
        super(vm, wrapped);
    }

}
