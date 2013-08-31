package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ObjectMirror;

public class WrappingMirror implements ObjectMirror {

    protected final WrappingVirtualMachine vm;
    protected final ObjectMirror wrapped;
    
    public WrappingMirror(WrappingVirtualMachine vm, ObjectMirror wrapped) {
        this.vm = vm;
        this.wrapped = wrapped;
        if (wrapped != null && vm.getWrappedVM() != wrapped.getClassMirror().getVM()) {
            throw new IllegalArgumentException();
        }
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
    
    public ObjectMirror getWrapped() {
        return wrapped;
    }
    
    
    @Override
    public WrappingClassMirror getClassMirror() {
        return vm.getWrappedClassMirror(wrapped.getClassMirror());
    }
    
    @Override
    public int identityHashCode() {
        return wrapped.identityHashCode();
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        if (wrapped instanceof ArrayMirror) {
            return vm.getWrappedMirror((ObjectMirror)((ArrayMirror)wrapped).clone());
        } else {
            throw new CloneNotSupportedException();
        }
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + wrapped;
    }
}
