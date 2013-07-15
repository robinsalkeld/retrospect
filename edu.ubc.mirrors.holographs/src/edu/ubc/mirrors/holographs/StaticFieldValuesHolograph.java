package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.StaticFieldValuesMirror;

public class StaticFieldValuesHolograph extends InstanceHolograph implements StaticFieldValuesMirror {

    private final StaticFieldValuesMirror wrapped;
    
    public StaticFieldValuesHolograph(VirtualMachineHolograph vm, StaticFieldValuesMirror wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }
    
    @Override
    public ClassHolograph forClassMirror() {
        return (ClassHolograph)vm.getWrappedMirror(wrapped.forClassMirror());
    }
}
