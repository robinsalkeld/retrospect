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
import java.util.concurrent.Callable;

import org.eclipse.jdi.TimeoutException;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

import edu.ubc.mirrors.Callback;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;

public class JDIMethodMirror extends JDIMethodOrConstructorMirror implements MethodMirror {

    public JDIMethodMirror(JDIVirtualMachineMirror vm, Method method) {
	super(vm, method);
	if (method.name().startsWith("<")) {
	    throw new IllegalArgumentException();
	}
    }
    
    @Override
    public byte[] getBytecode() {
        return method.bytecodes();
    }

    @Override
    protected ObjectReference getReflectiveInstance(final ThreadMirror thread) {
        try {
            return vm.withoutEventRequests(new Callable<ObjectReference>() {
                @Override
                public ObjectReference call() throws Exception {
                    return ((JDIObjectMirror)Reflection.methodInstanceForMethodMirror(thread, JDIMethodMirror.this)).getObjectReference();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public Object getDefaultValue(ThreadMirror thread) {
        ThreadReference threadRef = ((JDIThreadMirror)thread).thread;
        ObjectReference methodInstance = getReflectiveInstance(thread);
        ClassType methodClass = (ClassType)methodInstance.referenceType();
        Method getDefaultValueMethod = methodClass.methodsByName("getDefaultValue", "()Ljava/lang/Object;").get(0);
        Value defaultValue = vm.safeInvoke(methodInstance, threadRef, getDefaultValueMethod);
        return vm.wrapAnnotationValue(threadRef, defaultValue);
    }
    
    public String getName() {
	return method.name();
    }
    
    public Object invoke(ThreadMirror thread, ObjectMirror obj, Object... args)
	    throws IllegalArgumentException, IllegalAccessException,
	    MirrorInvocationTargetException {

        try {
            ThreadReference threadRef = ((JDIThreadMirror)thread).thread;
            List<Value> argValues = new ArrayList<Value>(args.length);
            for (Object arg : args) {
                argValues.add(vm.toValue(arg));
            }
            
            Value result;
            if (method.isStatic()) {
                ClassType klass = (ClassType)method.declaringType();
                result = klass.invokeMethod(threadRef, method,  argValues, 0);
            } else {
                ObjectReference objRef = ((JDIObjectMirror)obj).getObjectReference();
                result = objRef.invokeMethod(threadRef, method, argValues, 0);
            }
            
            return vm.wrapValue(result);
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        } catch (ClassNotLoadedException e) {
            throw new RuntimeException(e);
        } catch (IncompatibleThreadStateException e) {
            throw new RuntimeException(e);
        } catch (InvocationException e) {
            InstanceMirror eMirror = (InstanceMirror)vm.makeMirror(e.exception());
            throw new MirrorInvocationTargetException(eMirror);
        } catch (TimeoutException e) {
            Reflection.printAllThreads(vm);
            throw e;
        }
    }
    

    
}
