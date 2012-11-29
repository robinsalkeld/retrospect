package edu.ubc.mirrors.asjdi;

import com.sun.jdi.Type;
import com.sun.jdi.VoidValue;

public class MirrorsVoidValue extends MirrorsMirror implements VoidValue {

    private final Type type;
    
    public MirrorsVoidValue(MirrorsVirtualMachine vm) {
        super(vm, new Object());
        this.type = vm.typeForClassMirror(vm.vm.getPrimitiveClass("void"));
    }

    @Override
    public Type type() {
        return type;
    }

}
