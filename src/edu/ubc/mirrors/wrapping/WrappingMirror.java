package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ObjectMirror;

public class WrappingMirror implements ObjectMirror {

    protected final WrappingVirtualMachine vm;
    protected final ObjectMirror wrapped;
    
    public WrappingMirror(WrappingVirtualMachine vm, ObjectMirror wrapped) {
        this.vm = vm;
        this.wrapped = wrapped;
    }
    
    @Override
    public WrappingClassMirror getClassMirror() {
        return vm.getWrappedClassMirror(wrapped.getClassMirror());
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + wrapped;
    }
}
