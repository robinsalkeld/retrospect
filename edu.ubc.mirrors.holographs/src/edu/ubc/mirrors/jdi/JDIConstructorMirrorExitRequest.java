package edu.ubc.mirrors.jdi;

import com.sun.jdi.request.MethodExitRequest;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorExitRequest;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorExitRequest;

public class JDIConstructorMirrorExitRequest extends JDIEventRequest implements ConstructorMirrorExitRequest {

    protected final MethodExitRequest wrapped;
    protected ConstructorMirror constructorFilter;

    public JDIConstructorMirrorExitRequest(JDIVirtualMachineMirror vm, MethodExitRequest wrapped) {
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