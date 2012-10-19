package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorEntryRequest;

public class WrappingConstructorMirrorEntryRequest extends WrappingMirrorEventRequest implements ConstructorMirrorEntryRequest {

    private final ConstructorMirrorEntryRequest wrapped;
    
    public WrappingConstructorMirrorEntryRequest(WrappingVirtualMachine vm, ConstructorMirrorEntryRequest wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public void addClassFilter(ClassMirror klass) {
	wrapped.addClassFilter(vm.unwrapClassMirror(klass));
    }
    
    @Override
    public void setConstructorFilter(ConstructorMirror method) {
        wrapped.setConstructorFilter(((WrappingConstructorMirror)method).wrapped);
    }
    
}
