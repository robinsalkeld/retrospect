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
package edu.ubc.mirrors.raw.nativestubs.java.lang;

import static edu.ubc.mirrors.holograms.HologramClassGenerator.getOriginalBinaryClassName;

import java.lang.reflect.InvocationTargetException;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holograms.ObjectHologram;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class ThrowableStubs extends NativeStubs {

    public ThrowableStubs(ClassHolograph klass) {
	super(klass);
    }

    @StubMethod
    public InstanceMirror fillInStackTrace(InstanceMirror throwable) {
	try {
	    // TODO-RS: The abstraction leaks a bit here since Throwable is a tricky special case.
	    Object hologram = ObjectHologram.make(throwable);
	    hologram.getClass().getMethod("superFillInStackTrace").invoke(hologram);
	} catch (IllegalAccessException e) {
	    throw new IllegalAccessError(e.getMessage());
	} catch (InvocationTargetException e) {
	    throw new RuntimeException(e);
	} catch (NoSuchMethodException e) {
	    throw new NoSuchMethodError(e.getMessage());
	}
	
	// The native method has the side-effect of clearing the stackTrace field, so make sure the mirror does that too.
	HolographInternalUtils.setField(throwable, "stackTrace", null);
	
	return throwable;
    }
    
    // Java 1.7 version
    @StubMethod
    public InstanceMirror fillInStackTrace(InstanceMirror throwable, int dummy) {
	return fillInStackTrace(throwable);
    }
    
    private StackTraceElement[] getNativeStack(InstanceMirror throwable) {
	try {
	    return (StackTraceElement[])throwable.getClass().getMethod("superGetStackTrace").invoke(throwable);
	} catch (IllegalAccessException e) {
	    throw new IllegalAccessError(e.getMessage());
	} catch (InvocationTargetException e) {
	    throw new RuntimeException(e);
	} catch (NoSuchMethodException e) {
	    throw new NoSuchMethodError(e.getMessage());
	}
    }
    
    // TODO-RS: Pretty darn expensive, but caching this correctly is a bit tricky so leave that for later...
    @StubMethod
    public int getStackTraceDepth(InstanceMirror throwable) {
	return getNativeStack(throwable).length;
    }
    
    // TODO-RS: Pretty darn expensive, but caching this correctly is a bit tricky so leave that for later...
    @StubMethod
    public InstanceMirror getStackTraceElement(InstanceMirror throwable, int index) {
	VirtualMachineMirror vm = getVM();
        ClassMirror stackTraceElementClass = vm.findBootstrapClassMirror(StackTraceElement.class.getName());
        String stringClassName = String.class.getName();
        ConstructorMirror constructor = HolographInternalUtils.getConstructor(stackTraceElementClass, stringClassName, stringClassName, stringClassName, "int");
        
	StackTraceElement nativeFrame = getNativeStack(throwable)[index];
        InstanceMirror className = vm.makeString(getOriginalBinaryClassName(nativeFrame.getClassName()));
        InstanceMirror methodName = vm.makeString(nativeFrame.getMethodName());
        InstanceMirror fieldName = vm.makeString(nativeFrame.getFileName());
        int lineNumber = nativeFrame.getLineNumber();
        return HolographInternalUtils.newInstance(constructor, ThreadHolograph.currentThreadMirror(), className, methodName, fieldName, lineNumber);
    }
    
}
