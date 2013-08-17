package edu.ubc.mirrors.asjdi;

import com.sun.jdi.ByteType;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.asjdi.MirrorsType;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsByteType extends MirrorsType implements ByteType {

    public MirrorsByteType(MirrorsVirtualMachine vm, ClassMirror wrapped) {
        super(vm, wrapped);
    }

}
