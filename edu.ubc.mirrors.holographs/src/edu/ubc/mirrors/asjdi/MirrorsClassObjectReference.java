package edu.ubc.mirrors.asjdi;

import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ReferenceType;

import edu.ubc.mirrors.ClassMirror;

public class MirrorsClassObjectReference extends MirrorsObjectReference implements ClassObjectReference {

    public MirrorsClassObjectReference(MirrorsVirtualMachine vm, ClassMirror wrapped) {
        super(vm, wrapped);
    }

    @Override
    public ReferenceType reflectedType() {
        return (ReferenceType)vm.typeForClassMirror((ClassMirror)wrapped);
    }
}
