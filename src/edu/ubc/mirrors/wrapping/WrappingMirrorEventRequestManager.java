package edu.ubc.mirrors.wrapping;

import java.util.List;

import edu.ubc.mirrors.MethodMirrorEntryRequest;
import edu.ubc.mirrors.MethodMirrorExitRequest;
import edu.ubc.mirrors.MirrorEventRequestManager;

public class WrappingMirrorEventRequestManager implements MirrorEventRequestManager {

    private final WrappingVirtualMachine vm;
    private final MirrorEventRequestManager wrapped;
    
    public WrappingMirrorEventRequestManager(WrappingVirtualMachine vm, MirrorEventRequestManager wrapped) {
	this.vm = vm;
	this.wrapped = wrapped;
    }

    @Override
    public MethodMirrorEntryRequest createMethodMirrorEntryRequest() {
	return new WrappingMethodMirrorEntryRequest(vm, wrapped.createMethodMirrorEntryRequest());
    }

    @Override
    public List<MethodMirrorEntryRequest> methodMirrorEntryRequests() {
	throw new UnsupportedOperationException();
    }

    @Override
    public void deleteMethodMirrorEntryRequest(MethodMirrorEntryRequest request) {
	throw new UnsupportedOperationException();
    }

    @Override
    public MethodMirrorExitRequest createMethodMirrorExitRequest() {
	return new WrappingMethodMirrorExitRequest(vm, wrapped.createMethodMirrorExitRequest());
    }

    @Override
    public List<MethodMirrorExitRequest> methodMirrorExitRequests() {
	throw new UnsupportedOperationException();
    }

    @Override
    public void deleteMethodMirrorExitRequest(MethodMirrorExitRequest request) {
	throw new UnsupportedOperationException();
    }

}
