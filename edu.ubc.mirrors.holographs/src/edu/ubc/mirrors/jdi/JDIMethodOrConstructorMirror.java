package edu.ubc.mirrors.jdi;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Method;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;

public abstract class JDIMethodOrConstructorMirror extends JDIMirror {

    protected final Method method;
    
    public JDIMethodOrConstructorMirror(JDIVirtualMachineMirror vm, Method method) {
	super(vm, method);
	this.method = method;
    }

    public ClassMirror getDeclaringClass() {
	return vm.makeClassMirror(method.declaringType());
    }

    public int getSlot() {
	// TODO Auto-generated method stub
	return 0;
    }

    public int getModifiers() {
	return method.modifiers();
    }

    public List<String> getParameterTypeNames() {
        return method.argumentTypeNames();
    }
    
    public List<ClassMirror> getParameterTypes() {
	try {
	    return vm.makeClassMirrorList(method.argumentTypes());
	} catch (ClassNotLoadedException e) {
	    throw new UnsupportedOperationException();
	}
    }
    
    public List<String> getExceptionTypeNames() {
        throw new UnsupportedOperationException();
    }

    public List<ClassMirror> getExceptionTypes() {
	throw new UnsupportedOperationException();
    }

    public String getReturnTypeName() {
        return method.returnTypeName();
    }
    
    public ClassMirror getReturnType() {
	try {
	    return vm.makeClassMirror(method.returnType());
	} catch (ClassNotLoadedException e) {
	    throw new UnsupportedOperationException();
	}
    }

    public String getSignature() {
	return method.signature();
    }

    public void setAccessible(boolean flag) {
	// Ignore - I wonder if this shouldn't even be part of the mirrors API...
    }

    public byte[] getRawAnnotations() {
	throw new UnsupportedOperationException();
    }

    public byte[] getRawParameterAnnotations() {
	throw new UnsupportedOperationException();
    }

    public byte[] getRawAnnotationDefault() {
	throw new UnsupportedOperationException();
    }

}
