package edu.ubc.mirrors.asjdi;

import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ReferenceType;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.asjdi.MirrorsArrayType;
import edu.ubc.mirrors.asjdi.MirrorsClassType;
import edu.ubc.mirrors.asjdi.MirrorsInterfaceType;
import edu.ubc.mirrors.asjdi.MirrorsObjectReference;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsClassObjectReference extends MirrorsObjectReference implements ClassObjectReference {

    private final ReferenceType reflectedType;
    
    public MirrorsClassObjectReference(MirrorsVirtualMachine vm, ClassMirror wrapped) {
        super(vm, wrapped);
        if (wrapped.isArray()) {
            reflectedType = new MirrorsArrayType(vm, wrapped);
        } else if (wrapped.isInterface()) {
            reflectedType = new MirrorsInterfaceType(vm, wrapped);
        } else {
            reflectedType = new MirrorsClassType(vm, wrapped);
        }
    }

    @Override
    public ReferenceType reflectedType() {
        return reflectedType;
    }
}
