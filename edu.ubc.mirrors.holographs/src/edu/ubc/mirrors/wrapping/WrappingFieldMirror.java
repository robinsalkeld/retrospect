package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;

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
    public String getTypeName() {
        return wrapped.getTypeName();
    }
    
    @Override
    public int getModifiers() {
        return wrapped.getModifiers();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + wrapped;
    }
}
