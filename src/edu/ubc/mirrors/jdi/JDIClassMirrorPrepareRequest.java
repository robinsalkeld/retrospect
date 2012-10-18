package edu.ubc.mirrors.jdi;

import com.sun.jdi.request.ClassPrepareRequest;

import edu.ubc.mirrors.ClassMirrorPrepareRequest;

public class JDIClassMirrorPrepareRequest extends JDIEventRequest implements ClassMirrorPrepareRequest {

    protected final ClassPrepareRequest wrapped;
    
    public JDIClassMirrorPrepareRequest(JDIVirtualMachineMirror vm, ClassPrepareRequest wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }
    
    @Override
    public void addClassFilter(String classNamePattern) {
	wrapped.addClassFilter(classNamePattern);
    }
}
