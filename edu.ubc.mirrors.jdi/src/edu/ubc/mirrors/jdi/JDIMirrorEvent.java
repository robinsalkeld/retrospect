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

import com.sun.jdi.event.Event;
import com.sun.jdi.request.EventRequest;

import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorInvocationHandler;

public abstract class JDIMirrorEvent extends JDIMirror implements MirrorEvent {

    private final Event wrapped;
    
    public JDIMirrorEvent(JDIVirtualMachineMirror vm, Event wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public MirrorEventRequest request() {
        EventRequest request = wrapped.request();
        if (request == null) {
            return null;
        }
        return (MirrorEventRequest)request.getProperty(JDIEventRequest.MIRROR_WRAPPER);
    }
    
    @Override
    public List<Object> arguments() {
        return Collections.emptyList();
    }
    
    @Override
    public MirrorInvocationHandler getProceed() {
        return MirrorInvocationHandler.NONE;
    }
    
    @Override
    public void setProceed(MirrorInvocationHandler handler) {
        throw new UnsupportedOperationException();
    }
}
