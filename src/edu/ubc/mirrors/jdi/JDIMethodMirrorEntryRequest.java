package edu.ubc.mirrors.jdi;

import com.sun.jdi.request.MethodEntryRequest;

import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryRequest;

public class JDIMethodMirrorEntryRequest extends JDIEventRequest implements MethodMirrorEntryRequest {

    protected final MethodEntryRequest wrapped;
    private final MethodMirror method;

    public JDIMethodMirrorEntryRequest(JDIVirtualMachineMirror vm, MethodEntryRequest wrapped, MethodMirror method) {
	super(vm, wrapped);
	this.wrapped = wrapped;
	this.method = method;
    }

    @Override
    public MethodMirror method() {
	return method;
    }
    
}
