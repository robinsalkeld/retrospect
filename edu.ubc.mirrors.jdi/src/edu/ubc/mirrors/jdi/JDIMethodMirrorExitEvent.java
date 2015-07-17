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

import com.sun.jdi.event.MethodExitEvent;

import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorExitEvent;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;

public class JDIMethodMirrorExitEvent extends JDIMirrorEvent implements MethodMirrorExitEvent {

    private final MethodExitEvent wrapped;
    
    public JDIMethodMirrorExitEvent(JDIVirtualMachineMirror vm, MethodExitEvent wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public ThreadMirror thread() {
        return (ThreadMirror)vm.makeMirror(wrapped.thread());
    }
    
    @Override
    public MethodMirror method() {
        return new JDIMethodMirror(vm, wrapped.method());
    }
    
    @Override
    public Object returnValue() {
        return vm.wrapValue(wrapped.returnValue());
    }
    
    public static JDIMethodMirrorExitEvent wrap(JDIVirtualMachineMirror vm, MethodExitEvent mee) {
	JDIMethodMirrorExitEvent result = new JDIMethodMirrorExitEvent(vm, mee);
	Object request = mee.request().getProperty(JDIEventRequest.MIRROR_WRAPPER);
	if (!(request instanceof JDIMethodMirrorExitRequest)) {
	    return null;
	}
	JDIMethodMirrorExitRequest mmer = (JDIMethodMirrorExitRequest)request;
	// Apply the method filter if present, since it's not supported directly
        if (mmer.nameFilter != null) {
            if (!Reflection.methodMatches(result.method(), mmer.declaringClassFilter, mmer.nameFilter, mmer.parameterTypeNamesFilter)) {
                return null;
            }
        }
	return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " for " + Reflection.methodName(method());
    }
}
