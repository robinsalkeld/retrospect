package edu.ubc.mirrors.jdi;

import com.sun.jdi.request.MethodEntryRequest;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorEntryRequest;

public class JDIConstructorMirrorEntryRequest extends JDIEventRequest implements ConstructorMirrorEntryRequest {

    protected final MethodEntryRequest wrapped;
    protected ConstructorMirror constructorFilter;

    public JDIConstructorMirrorEntryRequest(JDIVirtualMachineMirror vm, MethodEntryRequest wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public void addClassFilter(ClassMirror klass) {
	JDIClassMirror declaringClass = (JDIClassMirror)klass;
	wrapped.addClassFilter(declaringClass.refType);
    }
    
    @Override
    public void setConstructorFilter(ConstructorMirror constructorFilter) {
	this.constructorFilter = constructorFilter;
	// Not supported directly, but adding a class filter helps to reduce excess events
	addClassFilter(constructorFilter.getDeclaringClass());
    }
    
    @Override
    public void addClassFilter(String classNamePattern) {
	wrapped.addClassFilter(classNamePattern);
    }
}
