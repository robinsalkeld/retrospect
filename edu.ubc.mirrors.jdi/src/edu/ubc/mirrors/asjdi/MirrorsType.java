package edu.ubc.mirrors.asjdi;

import com.sun.jdi.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.asjdi.MirrorsMirror;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsType extends MirrorsMirror implements Type {

    private final ClassMirror wrapped;
    
    public MirrorsType(MirrorsVirtualMachine vm, ClassMirror wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    @Override
    public String name() {
        return wrapped.getClassName();
    }

    @Override
    public String signature() {
        return Reflection.typeForClassMirror(wrapped).getDescriptor();
    }

}
