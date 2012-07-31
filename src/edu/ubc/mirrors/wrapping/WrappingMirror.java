package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpPrimitiveArrayMirror;
import edu.ubc.mirrors.test.Breakpoint;

public class WrappingMirror implements ObjectMirror, WrapperAware {

    protected final WrappingVirtualMachine vm;
    protected final ObjectMirror wrapped;
    
    public WrappingMirror(WrappingVirtualMachine vm, ObjectMirror wrapped) {
        this.vm = vm;
        this.wrapped = wrapped;
        if (wrapped != null && vm.getWrappedVM() != wrapped.getClassMirror().getVM()) {
            throw new IllegalArgumentException();
        }
        if (wrapped instanceof WrapperAware) {
            ((WrapperAware)wrapped).setWrapper(this);
        }
    }
    
    @Override
    public void setWrapper(WrappingMirror mirror) {
        if (wrapped instanceof WrapperAware) {
            ((WrapperAware)wrapped).setWrapper(mirror);
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
    public String toString() {
        return getClass().getSimpleName() + " on " + wrapped;
    }
}
