package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpPrimitiveArrayMirror;

public class WrappingMirror implements ObjectMirror {

    protected final WrappingVirtualMachine vm;
    protected final ObjectMirror wrapped;
    
    public WrappingMirror(WrappingVirtualMachine vm, ObjectMirror wrapped) {
        this.vm = vm;
        this.wrapped = wrapped;
    }
    
    @Override
    public final boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        
        return ((WrappingMirror)obj).wrapped.equals(wrapped);
    }
    
    @Override
    public final int hashCode() {
        return 47 * wrapped.hashCode();
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
