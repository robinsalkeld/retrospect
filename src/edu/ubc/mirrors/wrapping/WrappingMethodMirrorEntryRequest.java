package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryRequest;

public class WrappingMethodMirrorEntryRequest extends WrappingMirrorEventRequest implements MethodMirrorEntryRequest {

    private final MethodMirrorEntryRequest wrapped;
    
    public WrappingMethodMirrorEntryRequest(WrappingVirtualMachine vm, MethodMirrorEntryRequest wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public void addClassFilter(ClassMirror klass) {
	wrapped.addClassFilter(vm.unwrapClassMirror(klass));
    }
    
    @Override
    public void setMethodFilter(MethodMirror method) {
        wrapped.setMethodFilter(((WrappingMethodMirror)method).wrapped);
    }
    
}
