package edu.ubc.mirrors.wrapping;

import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorPrepareRequest;
import edu.ubc.mirrors.ConstructorMirrorEntryRequest;
import edu.ubc.mirrors.ConstructorMirrorExitRequest;
import edu.ubc.mirrors.FieldMirrorSetRequest;
import edu.ubc.mirrors.MethodMirrorEntryRequest;
import edu.ubc.mirrors.MethodMirrorExitRequest;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.ThreadMirrorDeathRequest;
import edu.ubc.mirrors.ThreadMirrorStartRequest;

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
    public MethodMirrorExitRequest createMethodMirrorExitRequest() {
	return new WrappingMethodMirrorExitRequest(vm, wrapped.createMethodMirrorExitRequest());
    }

    @Override
    public List<MethodMirrorExitRequest> methodMirrorExitRequests() {
	throw new UnsupportedOperationException();
    }

    @Override
    public ConstructorMirrorEntryRequest createConstructorMirrorEntryRequest() {
	return new WrappingConstructorMirrorEntryRequest(vm, wrapped.createConstructorMirrorEntryRequest());
    }

    @Override
    public List<ConstructorMirrorEntryRequest> constructorMirrorEntryRequests() {
	throw new UnsupportedOperationException();
    }

    @Override
    public ConstructorMirrorExitRequest createConstructorMirrorExitRequest() {
	return new WrappingConstructorMirrorExitRequest(vm, wrapped.createConstructorMirrorExitRequest());
    }

    @Override
    public List<ConstructorMirrorExitRequest> constructorMirrorExitRequests() {
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
    public ClassMirrorPrepareRequest createClassMirrorPrepareRequest() {
	return new WrappingClassMirrorPrepareRequest(vm, wrapped.createClassMirrorPrepareRequest());
    }

    @Override
    public List<ClassMirrorPrepareRequest> classMirrorPrepareRequests() {
	throw new UnsupportedOperationException();
    }

    @Override
    public ThreadMirrorDeathRequest createThreadMirrorDeathRequest() {
        return new WrappingThreadMirrorDeathRequest(vm, wrapped.createThreadMirrorDeathRequest());
    }
    
    @Override
    public ThreadMirrorStartRequest createThreadMirrorStartRequest() {
        return new WrappingThreadMirrorStartRequest(vm, wrapped.createThreadMirrorStartRequest());
    }
    
    @Override
    public void deleteMirrorEventRequest(MirrorEventRequest request) {
	throw new UnsupportedOperationException();
    }
}
