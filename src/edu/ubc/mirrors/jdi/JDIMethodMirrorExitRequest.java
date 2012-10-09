package edu.ubc.mirrors.jdi;

import com.sun.jdi.request.MethodExitRequest;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorExitRequest;

public class JDIMethodMirrorExitRequest extends JDIEventRequest implements MethodMirrorExitRequest {

    protected final MethodExitRequest wrapped;
    protected MethodMirror methodFilter;

    public JDIMethodMirrorExitRequest(JDIVirtualMachineMirror vm, MethodExitRequest wrapped) {
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