package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryRequest;

public class WrappingMethodMirrorEntryRequest extends WrappingMirrorEventRequest implements MethodMirrorEntryRequest {

    private final MethodMirrorEntryRequest wrapped;
    
    public WrappingMethodMirrorEntryRequest(WrappingVirtualMachine vm, MethodMirrorEntryRequest wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public MethodMirror method() {
	return new WrappingMethodMirror(vm, wrapped.method());
    }

}
