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

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

import edu.ubc.mirrors.AnnotationMirror;
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

        ThreadReference threadRef = ((JDIThreadMirror)thread).thread;
        ObjectReference objRef = ((JDIObjectMirror)obj).mirror;
        
        List<Value> argValues = new ArrayList<Value>(args.length);
        for (Object arg : args) {
            argValues.add(vm.toValue(arg));
        }
        
        try {
            Value result = objRef.invokeMethod(threadRef, method, argValues, ObjectReference.INVOKE_SINGLE_THREADED);
            return vm.wrapValue(result);
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        } catch (ClassNotLoadedException e) {
            throw new RuntimeException(e);
        } catch (IncompatibleThreadStateException e) {
            throw new RuntimeException(e);
        } catch (InvocationException e) {
            // TODO-RS: Use MirrorInvocationTargetException?
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public List<AnnotationMirror> getAnnotations() {
        throw new UnsupportedOperationException();
    }
}
