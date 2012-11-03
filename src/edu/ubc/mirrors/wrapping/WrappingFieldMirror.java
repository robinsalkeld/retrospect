package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpClassMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.test.Breakpoint;

public class WrappingFieldMirror implements FieldMirror {

    private final WrappingVirtualMachine vm;
    protected final FieldMirror wrapped;
    
    protected WrappingFieldMirror(WrappingVirtualMachine vm, FieldMirror wrapped) {
        this.vm = vm;
        this.wrapped = wrapped;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WrappingFieldMirror)) {
            return false;
        }
        
        return wrapped.equals(((WrappingFieldMirror)obj).wrapped);
    }
    
    @Override
    public int hashCode() {
        return 11 * wrapped.hashCode();
    }
    
    @Override
    public ClassMirror getDeclaringClass() {
        return vm.getWrappedClassMirror(wrapped.getDeclaringClass());
    }
    
    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public ClassMirror getType() {
        return vm.getWrappedClassMirror(wrapped.getType());
    }

    @Override
    public int getModifiers() {
        return wrapped.getModifiers();
    }
}
