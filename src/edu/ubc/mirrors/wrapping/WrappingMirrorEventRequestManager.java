package edu.ubc.mirrors.wrapping;

import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorPrepareRequest;
import edu.ubc.mirrors.FieldMirrorSetRequest;
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

    @Override
    public FieldMirrorSetRequest createFieldMirrorSetRequest(ClassMirror klass, String fieldName) {
	ClassMirror unwrappedClass = vm.unwrapClassMirror(klass);
	return new WrappingFieldMirrorSetRequest(vm, wrapped.createFieldMirrorSetRequest(unwrappedClass, fieldName));
    }

    @Override
    public List<FieldMirrorSetRequest> fieldMirrorSetRequests() {
	throw new UnsupportedOperationException();
    }

    @Override
    public void deleteFieldMirrorSetRequest(FieldMirrorSetRequest request) {
	throw new UnsupportedOperationException();
    }

    @Override
    public ClassMirrorPrepareRequest createClassMirrorPrepareRequest() {
	return new WrappingClassMirrorPrepareRequest(vm, wrapped.createClassMirrorPrepareRequest());
    }

    @Override
    public List<ClassMirrorPrepareRequest> classMirrorPrepareRequests() {
	throw new UnsupportedOperationException();
    }

    @Override
    public void deleteClassMirrorPrepareRequest(ClassMirrorPrepareRequest request) {
	throw new UnsupportedOperationException();
    }
}
