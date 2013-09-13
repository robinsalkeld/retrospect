/*******************************************************************************
 * Copyright (c) 2013 Robin Salkeld
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package edu.ubc.mirrors.jdi;

import java.util.List;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Method;

import edu.ubc.mirrors.ClassMirror;

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
