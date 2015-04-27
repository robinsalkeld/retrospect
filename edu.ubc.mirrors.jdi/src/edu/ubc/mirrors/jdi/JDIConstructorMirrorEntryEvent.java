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

import java.util.Collections;
import java.util.List;

import com.sun.jdi.event.MethodEntryEvent;

import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorEntryEvent;
import edu.ubc.mirrors.ThreadMirror;

public class JDIConstructorMirrorEntryEvent extends JDIMirrorEvent implements ConstructorMirrorEntryEvent {

    private final MethodEntryEvent wrapped;
    
    public JDIConstructorMirrorEntryEvent(JDIVirtualMachineMirror vm, MethodEntryEvent wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public ConstructorMirror constructor() {
        return new JDIConstructorMirror(vm, wrapped.method());
    }
    
    public static JDIConstructorMirrorEntryEvent wrap(JDIVirtualMachineMirror vm, MethodEntryEvent mee) {
	JDIConstructorMirrorEntryEvent result = new JDIConstructorMirrorEntryEvent(vm, mee);
	Object request = mee.request().getProperty(JDIEventRequest.MIRROR_WRAPPER);
	if (!(request instanceof JDIConstructorMirrorEntryRequest)) {
	    return null;
	}
	JDIConstructorMirrorEntryRequest cmer = (JDIConstructorMirrorEntryRequest)request;
	// Apply the constructor filter if present, since it's not supported directly
	if (cmer.constructorFilter != null && !cmer.constructorFilter.equals(result.constructor())) {
	    return null;
	}
	return result;
    }
    
    @Override
    public ThreadMirror thread() {
        return (ThreadMirror)vm.makeMirror(wrapped.thread());
    }
    
    @Override
    public List<Object> arguments() {
        List<Object> arguments = thread().getStackTrace().get(0).arguments();
        return arguments != null ? arguments : Collections.emptyList();
    }
}
