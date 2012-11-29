package edu.ubc.mirrors.jdi;

import java.lang.reflect.InvocationTargetException;

import com.sun.jdi.Method;

import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;

public class JDIMethodMirror extends JDIMethodOrConstructorMirror implements MethodMirror {

    public JDIMethodMirror(JDIVirtualMachineMirror vm, Method method) {
	super(vm, method);
	if (method.name().startsWith("<")) {
	    throw new IllegalArgumentException();
	}
    }

    public String getName() {
	return method.name();
    }
    
    public Object invoke(ThreadMirror thread, ObjectMirror obj, Object... args)
	    throws IllegalArgumentException, IllegalAccessException,
	    MirrorInvocationTargetException {

	// TODO-RS: Implementable but not needed.
	throw new UnsupportedOperationException();
    }
}
