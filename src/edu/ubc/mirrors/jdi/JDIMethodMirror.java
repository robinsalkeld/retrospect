package edu.ubc.mirrors.jdi;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Method;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;

public class JDIMethodMirror extends JDIMirror implements MethodMirror {

    private final Method method;
    
    public JDIMethodMirror(JDIVirtualMachineMirror vm, Method method) {
	super(vm, method);
	this.method = method;
    }

    @Override
    public ClassMirror getDeclaringClass() {
	return vm.makeClassMirror(method.declaringType());
    }

    @Override
    public int getSlot() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public int getModifiers() {
	return method.modifiers();
    }

    @Override
    public String getName() {
	return method.name();
    }

    @Override
    public List<ClassMirror> getParameterTypes() {
	try {
	    return vm.makeClassMirrorList(method.argumentTypes());
	} catch (ClassNotLoadedException e) {
	    throw new UnsupportedOperationException();
	}
    }

    @Override
    public List<ClassMirror> getExceptionTypes() {
	throw new UnsupportedOperationException();
    }

    @Override
    public ClassMirror getReturnType() {
	try {
	    return vm.makeClassMirror(method.returnType());
	} catch (ClassNotLoadedException e) {
	    throw new UnsupportedOperationException();
	}
    }

    @Override
    public String getSignature() {
	return method.signature();
    }

    @Override
    public Object invoke(ThreadMirror thread, ObjectMirror obj, Object... args)
	    throws IllegalArgumentException, IllegalAccessException,
	    InvocationTargetException {

	// TODO-RS: Implementable but not needed.
	throw new UnsupportedOperationException();
    }

    @Override
    public void setAccessible(boolean flag) {
	// Ignore - I wonder if this shouldn't even be part of the mirrors API...
    }

    @Override
    public byte[] getRawAnnotations() {
	throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getRawParameterAnnotations() {
	throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getRawAnnotationDefault() {
	throw new UnsupportedOperationException();
    }

}
