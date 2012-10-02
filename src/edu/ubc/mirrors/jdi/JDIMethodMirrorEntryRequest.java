package edu.ubc.mirrors.jdi;

import com.sun.jdi.request.MethodEntryRequest;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryRequest;

public class JDIMethodMirrorEntryRequest extends JDIEventRequest implements MethodMirrorEntryRequest {

    protected final MethodEntryRequest wrapped;
    protected MethodMirror methodFilter;

    public JDIMethodMirrorEntryRequest(JDIVirtualMachineMirror vm, MethodEntryRequest wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public void addClassFilter(ClassMirror klass) {
	JDIClassMirror declaringClass = (JDIClassMirror)klass;
	wrapped.addClassFilter(declaringClass.refType);
    }
    
    @Override
    public void setMethodFilter(MethodMirror methodFilter) {
	this.methodFilter = methodFilter;
	// Not supported directly, but adding a class filter helps to reduce excess events
	addClassFilter(methodFilter.getDeclaringClass());
    }
}
