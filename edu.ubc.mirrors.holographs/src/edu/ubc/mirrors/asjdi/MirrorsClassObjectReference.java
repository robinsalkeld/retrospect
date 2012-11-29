package edu.ubc.mirrors.asjdi;

import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ReferenceType;

import edu.ubc.mirrors.ClassMirror;

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
