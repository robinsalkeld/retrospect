package edu.ubc.mirrors.asjdi;

import com.sun.jdi.ShortType;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.asjdi.MirrorsType;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsShortType extends MirrorsType implements ShortType {

    public MirrorsShortType(MirrorsVirtualMachine vm, ClassMirror wrapped) {
        super(vm, wrapped);
    }

}
