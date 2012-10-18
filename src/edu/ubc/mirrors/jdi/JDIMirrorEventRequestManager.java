package edu.ubc.mirrors.jdi;

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.Field;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import com.sun.jdi.request.ModificationWatchpointRequest;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorPrepareRequest;
import edu.ubc.mirrors.FieldMirrorSetRequest;
import edu.ubc.mirrors.MethodMirrorEntryRequest;
import edu.ubc.mirrors.MethodMirrorExitRequest;
import edu.ubc.mirrors.MirrorEventRequestManager;

public class JDIMirrorEventRequestManager implements MirrorEventRequestManager {

    private final JDIVirtualMachineMirror vm;
    private final EventRequestManager wrapped;
    
    public JDIMirrorEventRequestManager(JDIVirtualMachineMirror vm, EventRequestManager wrapped) {
	this.vm = vm;
	this.wrapped = wrapped;
    }

    @Override
    public MethodMirrorEntryRequest createMethodMirrorEntryRequest() {
	return new JDIMethodMirrorEntryRequest(vm, wrapped.createMethodEntryRequest());
    }

    @Override
    public List<MethodMirrorEntryRequest> methodMirrorEntryRequests() {
	List<MethodMirrorEntryRequest> result = new ArrayList<MethodMirrorEntryRequest>();
	for (MethodEntryRequest r : wrapped.methodEntryRequests()) {
	    result.add((MethodMirrorEntryRequest)r.getProperty(JDIEventRequest.MIRROR_WRAPPER));
	}
	return result;
    }

    @Override
    public void deleteMethodMirrorEntryRequest(MethodMirrorEntryRequest request) {
	MethodEntryRequest unwrapped = ((JDIMethodMirrorEntryRequest)request).wrapped;
	wrapped.deleteEventRequest(unwrapped);
    }

    @Override
    public MethodMirrorExitRequest createMethodMirrorExitRequest() {
	return new JDIMethodMirrorExitRequest(vm, wrapped.createMethodExitRequest());
    }

    @Override
    public List<MethodMirrorExitRequest> methodMirrorExitRequests() {
	List<MethodMirrorExitRequest> result = new ArrayList<MethodMirrorExitRequest>();
	for (MethodExitRequest r : wrapped.methodExitRequests()) {
	    result.add((MethodMirrorExitRequest)r.getProperty(JDIEventRequest.MIRROR_WRAPPER));
	}
	return result;
    }

    @Override
    public void deleteMethodMirrorExitRequest(MethodMirrorExitRequest request) {
	MethodExitRequest unwrapped = ((JDIMethodMirrorExitRequest)request).wrapped;
	wrapped.deleteEventRequest(unwrapped);
    }

    @Override
    public FieldMirrorSetRequest createFieldMirrorSetRequest(ClassMirror klass, String fieldName) {
	// TODO-RS: Doh again on fixing the field API
	Field f = ((JDIClassMirror)klass).refType.fieldByName(fieldName);
	return new JDIFieldMirrorSetRequest(vm, wrapped.createModificationWatchpointRequest(f));
    }

    @Override
    public List<FieldMirrorSetRequest> fieldMirrorSetRequests() {
	List<FieldMirrorSetRequest> result = new ArrayList<FieldMirrorSetRequest>();
	for (ModificationWatchpointRequest r : wrapped.modificationWatchpointRequests()) {
	    result.add((FieldMirrorSetRequest)r.getProperty(JDIEventRequest.MIRROR_WRAPPER));
	}
	return result;
    }

    @Override
    public void deleteFieldMirrorSetRequest(FieldMirrorSetRequest request) {
	ModificationWatchpointRequest unwrapped = ((JDIFieldMirrorSetRequest)request).wrapped;
	wrapped.deleteEventRequest(unwrapped);
    }

    @Override
    public ClassMirrorPrepareRequest createClassMirrorPrepareRequest() {
	return new JDIClassMirrorPrepareRequest(vm, wrapped.createClassPrepareRequest());
    }

    @Override
    public List<ClassMirrorPrepareRequest> classMirrorPrepareRequests() {
	List<ClassMirrorPrepareRequest> result = new ArrayList<ClassMirrorPrepareRequest>();
	for (ClassPrepareRequest r : wrapped.classPrepareRequests()) {
	    result.add((ClassMirrorPrepareRequest)r.getProperty(JDIEventRequest.MIRROR_WRAPPER));
	}
	return result;
    }

    @Override
    public void deleteClassMirrorPrepareRequest(ClassMirrorPrepareRequest request) {
	ClassPrepareRequest unwrapped = ((JDIClassMirrorPrepareRequest)request).wrapped;
	wrapped.deleteEventRequest(unwrapped);
    }
}
