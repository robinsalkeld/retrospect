package edu.ubc.mirrors.wrapping;

import java.util.List;

import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ThreadMirror;

public class WrappingAnnotationMirror implements AnnotationMirror {

    private final WrappingVirtualMachine vm;
    private final AnnotationMirror wrapped;
    
    public WrappingAnnotationMirror(WrappingVirtualMachine vm, AnnotationMirror wrapped) {
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
    public ClassMirror getClassMirror() {
        return vm.getWrappedClassMirror(wrapped.getClassMirror());
    }

    @Override
    public List<String> getKeys() {
        return wrapped.getKeys();
    }
    
    @Override
    public Object getValue(ThreadMirror thread, String name) {
        ThreadMirror unwrappedThread = (ThreadMirror)vm.unwrapInstanceMirror(thread);
        Object value = wrapped.getValue(unwrappedThread, name);
        return vm.wrapValue(value);
    }
}
