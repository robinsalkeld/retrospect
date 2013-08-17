package edu.ubc.mirrors.asjdi;

import com.sun.jdi.StringReference;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.asjdi.MirrorsObjectReference;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsStringReference extends MirrorsObjectReference implements StringReference {

    public MirrorsStringReference(MirrorsVirtualMachine vm, ObjectMirror wrapped) {
        super(vm, wrapped);
    }

    @Override
    public String value() {
        return Reflection.getRealStringForMirror((InstanceMirror)wrapped);
    }

}
