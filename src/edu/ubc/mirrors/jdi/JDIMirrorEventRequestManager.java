package edu.ubc.mirrors.jdi;

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;

import edu.ubc.mirrors.MethodMirror;
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

}
