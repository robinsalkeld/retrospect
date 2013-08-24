package edu.ubc.mirrors.asjdi;

import com.sun.jdi.FloatType;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.asjdi.MirrorsType;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsFloatType extends MirrorsType implements FloatType {

    public MirrorsFloatType(MirrorsVirtualMachine vm, ClassMirror wrapped) {
        super(vm, wrapped);
    }

}