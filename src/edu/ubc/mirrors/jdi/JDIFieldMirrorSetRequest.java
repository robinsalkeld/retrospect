package edu.ubc.mirrors.jdi;

import com.sun.jdi.request.ModificationWatchpointRequest;

import edu.ubc.mirrors.FieldMirrorSetRequest;

public class JDIFieldMirrorSetRequest extends JDIEventRequest implements FieldMirrorSetRequest {

    protected final ModificationWatchpointRequest wrapped;
    
    public JDIFieldMirrorSetRequest(JDIVirtualMachineMirror vm, ModificationWatchpointRequest wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public void addClassFilter(String classNamePattern) {
	wrapped.addClassFilter(classNamePattern);
    }

}
