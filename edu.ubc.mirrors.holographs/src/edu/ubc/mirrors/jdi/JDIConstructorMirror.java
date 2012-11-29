package edu.ubc.mirrors.jdi;

import com.sun.jdi.Method;

import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ThreadMirror;

public class JDIConstructorMirror extends JDIMethodOrConstructorMirror implements ConstructorMirror {

    public JDIConstructorMirror(JDIVirtualMachineMirror vm, Method method) {
	super(vm, method);
	if (!method.name().startsWith("<init>")) {
	    throw new IllegalArgumentException();
	}
    }
    
    @Override
    public InstanceMirror newInstance(ThreadMirror thread, Object... args)
            throws IllegalAccessException, IllegalArgumentException, MirrorInvocationTargetException {
	
	// TODO-RS: Implementable but not needed.
	throw new UnsupportedOperationException();
    }
}