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

import com.sun.jdi.Field;
import com.sun.jdi.Location;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import com.sun.jdi.request.ModificationWatchpointRequest;

import edu.ubc.mirrors.ClassMirrorPrepareRequest;
import edu.ubc.mirrors.ConstructorMirrorEntryRequest;
import edu.ubc.mirrors.ConstructorMirrorExitRequest;
import edu.ubc.mirrors.ConstructorMirrorHandlerRequest;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FieldMirrorGetHandlerRequest;
import edu.ubc.mirrors.FieldMirrorGetRequest;
import edu.ubc.mirrors.FieldMirrorSetHandlerRequest;
import edu.ubc.mirrors.FieldMirrorSetRequest;
import edu.ubc.mirrors.MethodMirrorEntryRequest;
import edu.ubc.mirrors.MethodMirrorExitRequest;
import edu.ubc.mirrors.MethodMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorLocation;
import edu.ubc.mirrors.MirrorLocationRequest;
import edu.ubc.mirrors.ThreadMirrorDeathRequest;
import edu.ubc.mirrors.ThreadMirrorStartRequest;

public class JDIMirrorEventRequestManager implements MirrorEventRequestManager {

    private final JDIVirtualMachineMirror vm;
    private final EventRequestManager wrapped;
    
    public JDIMirrorEventRequestManager(JDIVirtualMachineMirror vm, EventRequestManager wrapped) {
	this.vm = vm;
	this.wrapped = wrapped;
    }

    @Override
    public MirrorLocationRequest createLocationRequest(MirrorLocation location) {
        Location jdiLoc = ((JDIMirrorLocation)location).getWrapped();
        return new JDIMirrorLocationRequest(vm, wrapped.createBreakpointRequest(jdiLoc));
    }
    
    @Override
    public List<MirrorLocationRequest> locationRequests() {
        List<MirrorLocationRequest> result = new ArrayList<MirrorLocationRequest>();
        for (BreakpointRequest r : wrapped.breakpointRequests()) {
            Object wrapper = r.getProperty(JDIEventRequest.MIRROR_WRAPPER);
            if (wrapper instanceof MirrorLocationRequest) {
                result.add((MirrorLocationRequest)wrapper);
            }
        }
        return result;
    }
    
    @Override
    public MethodMirrorEntryRequest createMethodMirrorEntryRequest() {
	return new JDIMethodMirrorEntryRequest(vm, wrapped.createMethodEntryRequest());
    }

    @Override
    public List<MethodMirrorEntryRequest> methodMirrorEntryRequests() {
	List<MethodMirrorEntryRequest> result = new ArrayList<MethodMirrorEntryRequest>();
	for (MethodEntryRequest r : wrapped.methodEntryRequests()) {
	    Object wrapper = r.getProperty(JDIEventRequest.MIRROR_WRAPPER);
	    if (wrapper instanceof MethodMirrorEntryRequest) {
		result.add((MethodMirrorEntryRequest)wrapper);
	    }
	}
	return result;
    }

    @Override
    public MethodMirrorExitRequest createMethodMirrorExitRequest() {
	return new JDIMethodMirrorExitRequest(vm, wrapped.createMethodExitRequest());
    }

    @Override
    public List<MethodMirrorExitRequest> methodMirrorExitRequests() {
	List<MethodMirrorExitRequest> result = new ArrayList<MethodMirrorExitRequest>();
	for (MethodExitRequest r : wrapped.methodExitRequests()) {
	    Object wrapper = r.getProperty(JDIEventRequest.MIRROR_WRAPPER);
	    if (wrapper instanceof MethodMirrorExitRequest) {
		result.add((MethodMirrorExitRequest)wrapper);
	    }
	}
	return result;
    }
    
    @Override
    public ConstructorMirrorEntryRequest createConstructorMirrorEntryRequest() {
	return new JDIConstructorMirrorEntryRequest(vm, wrapped.createMethodEntryRequest());
    }

    @Override
    public List<ConstructorMirrorEntryRequest> constructorMirrorEntryRequests() {
	List<ConstructorMirrorEntryRequest> result = new ArrayList<ConstructorMirrorEntryRequest>();
	for (MethodEntryRequest r : wrapped.methodEntryRequests()) {
	    Object wrapper = r.getProperty(JDIEventRequest.MIRROR_WRAPPER);
	    if (wrapper instanceof ConstructorMirrorEntryRequest) {
		result.add((ConstructorMirrorEntryRequest)wrapper);
	    }
	}
	return result;
    }

    @Override
    public ConstructorMirrorExitRequest createConstructorMirrorExitRequest() {
	return new JDIConstructorMirrorExitRequest(vm, wrapped.createMethodExitRequest());
    }

    @Override
    public List<ConstructorMirrorExitRequest> constructorMirrorExitRequests() {
	List<ConstructorMirrorExitRequest> result = new ArrayList<ConstructorMirrorExitRequest>();
	for (MethodExitRequest r : wrapped.methodExitRequests()) {
	    Object wrapper = r.getProperty(JDIEventRequest.MIRROR_WRAPPER);
	    if (wrapper instanceof ConstructorMirrorExitRequest) {
		result.add((ConstructorMirrorExitRequest)wrapper);
	    }
	}
	return result;
    }

    @Override
    public FieldMirrorGetRequest createFieldMirrorGetRequest(FieldMirror fieldMirror) {
        Field f = ((JDIFieldMirror)fieldMirror).field;
        return new JDIFieldMirrorGetRequest(vm, wrapped.createAccessWatchpointRequest(f));
    }

    @Override
    public List<FieldMirrorGetRequest> fieldMirrorGetRequests() {
        List<FieldMirrorGetRequest> result = new ArrayList<FieldMirrorGetRequest>();
        for (ModificationWatchpointRequest r : wrapped.modificationWatchpointRequests()) {
            result.add((FieldMirrorGetRequest)r.getProperty(JDIEventRequest.MIRROR_WRAPPER));
        }
        return result;
    }

    @Override
    public FieldMirrorSetRequest createFieldMirrorSetRequest(FieldMirror fieldMirror) {
	Field f = ((JDIFieldMirror)fieldMirror).field;
	return new JDIFieldMirrorSetRequest(vm, wrapped.createModificationWatchpointRequest(f));
    }

    @Override
    public List<FieldMirrorSetRequest> fieldMirrorSetRequests() {
	List<FieldMirrorSetRequest> result = new ArrayList<FieldMirrorSetRequest>();
	for (ModificationWatchpointRequest r : wrapped.modificationWatchpointRequests()) {
	    result.add((FieldMirrorSetRequest)r.getProperty(JDIEventRequest.MIRROR_WRAPPER));
	}
	return result;
    }

    @Override
    public ClassMirrorPrepareRequest createClassMirrorPrepareRequest() {
	return new JDIClassMirrorPrepareRequest(vm, wrapped.createClassPrepareRequest());
    }

    @Override
    public List<ClassMirrorPrepareRequest> classMirrorPrepareRequests() {
	List<ClassMirrorPrepareRequest> result = new ArrayList<ClassMirrorPrepareRequest>();
	for (ClassPrepareRequest r : wrapped.classPrepareRequests()) {
	    result.add((ClassMirrorPrepareRequest)r.getProperty(JDIEventRequest.MIRROR_WRAPPER));
	}
	return result;
    }

    @Override
    public void deleteMirrorEventRequest(MirrorEventRequest request) {
	EventRequest unwrapped = ((JDIEventRequest)request).wrapped;
	wrapped.deleteEventRequest(unwrapped);
    }

    @Override
    public ThreadMirrorDeathRequest createThreadMirrorDeathRequest() {
        return new JDIThreadMirrorDeathRequest(vm, wrapped.createThreadDeathRequest());
    }
    
    @Override
    public ThreadMirrorStartRequest createThreadMirrorStartRequest() {
        return new JDIThreadMirrorStartRequest(vm, wrapped.createThreadStartRequest());
    }
    
    @Override
    public MethodMirrorHandlerRequest createMethodMirrorHandlerRequest() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ConstructorMirrorHandlerRequest createConstructorMirrorHandlerRequest() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public FieldMirrorSetHandlerRequest createFieldMirrorSetHandlerRequest(FieldMirror field) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public FieldMirrorGetHandlerRequest createFieldMirrorGetHandlerRequest(FieldMirror field) {
        throw new UnsupportedOperationException();
    }
}
