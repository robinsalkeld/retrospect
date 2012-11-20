package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorExitRequest;

public class WrappingMethodMirrorExitRequest extends WrappingMirrorEventRequest implements MethodMirrorExitRequest {
    
    private final MethodMirrorExitRequest wrapped;
    
    public WrappingMethodMirrorExitRequest(WrappingVirtualMachine vm, MethodMirrorExitRequest wrapped) {
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
