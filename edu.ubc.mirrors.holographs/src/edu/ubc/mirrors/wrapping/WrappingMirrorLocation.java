package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MirrorLocation;

public class WrappingMirrorLocation implements MirrorLocation {

    private final WrappingVirtualMachine vm;
    private final MirrorLocation wrapped;
    
    public WrappingMirrorLocation(WrappingVirtualMachine vm, MirrorLocation wrapped) {
        this.vm = vm;
        this.wrapped = wrapped;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        
        return wrapped.equals(((WrappingMirrorLocation)obj).wrapped);
    }
    
    @Override
    public int hashCode() {
        return 7 * wrapped.hashCode();
    }
    
    public MirrorLocation getWrapped() {
        return wrapped;
    }
    
    @Override
    public ClassMirror declaringClass() {
        return vm.getWrappedClassMirror(wrapped.declaringClass());
    }
}
