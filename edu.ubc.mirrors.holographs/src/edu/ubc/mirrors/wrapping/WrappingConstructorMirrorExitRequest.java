package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorExitRequest;

public class WrappingConstructorMirrorExitRequest extends WrappingMirrorEventRequest implements ConstructorMirrorExitRequest {
    
    private final ConstructorMirrorExitRequest wrapped;
    
    public WrappingConstructorMirrorExitRequest(WrappingVirtualMachine vm, ConstructorMirrorExitRequest wrapped) {
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
