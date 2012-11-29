package edu.ubc.mirrors.asjdi;

import com.sun.jdi.StringReference;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.mirages.Reflection;

public class MirrorsStringReference extends MirrorsObjectReference implements StringReference {

    public MirrorsStringReference(MirrorsVirtualMachine vm, ObjectMirror wrapped) {
        super(vm, wrapped);
    }

    @Override
    public String value() {
        return Reflection.getRealStringForMirror((InstanceMirror)wrapped);
    }

}
