package edu.ubc.mirrors.asjdi;

import com.sun.jdi.Type;
import com.sun.jdi.VoidValue;

import edu.ubc.mirrors.asjdi.MirrorsMirror;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

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
