package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.StaticFieldValuesMirror;

public class WrappingStaticFieldValuesMirror extends WrappingInstanceMirror implements StaticFieldValuesMirror {

    private final StaticFieldValuesMirror wrapped;
    
    public WrappingStaticFieldValuesMirror(WrappingVirtualMachine vm, StaticFieldValuesMirror wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }
    
    @Override
    public ClassMirror forClassMirror() {
        return (ClassMirror)vm.getWrappedMirror(wrapped.forClassMirror());
    }
}
