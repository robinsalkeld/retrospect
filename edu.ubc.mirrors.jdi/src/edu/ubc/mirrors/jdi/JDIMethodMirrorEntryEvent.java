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

import com.sun.jdi.event.MethodEntryEvent;

import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryEvent;
import edu.ubc.mirrors.ThreadMirror;

public class JDIMethodMirrorEntryEvent extends JDIMirrorEvent implements MethodMirrorEntryEvent {

    private final MethodEntryEvent wrapped;
    
    public JDIMethodMirrorEntryEvent(JDIVirtualMachineMirror vm, MethodEntryEvent wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public ThreadMirror thread() {
        return new JDIThreadMirror(vm, wrapped.thread());
    }
    
    @Override
    public MethodMirror method() {
        return new JDIMethodMirror(vm, wrapped.method());
    }
    
    public static JDIMethodMirrorEntryEvent wrap(JDIVirtualMachineMirror vm, MethodEntryEvent wrapped) {
	Object request = wrapped.request().getProperty(JDIEventRequest.MIRROR_WRAPPER);
	if (!(request instanceof JDIMethodMirrorEntryRequest)) {
	    return null;
	}
	JDIMethodMirrorEntryEvent result = new JDIMethodMirrorEntryEvent(vm, wrapped);
	JDIMethodMirrorEntryRequest mmer = (JDIMethodMirrorEntryRequest)request;
	// Apply the method filter if present, since it's not supported directly
	if (mmer.methodFilter != null && !mmer.methodFilter.equals(result.method())) {
	    return null;
	}
	return result;
    }
}
