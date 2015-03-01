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
package edu.ubc.mirrors.wrapping;

import java.util.List;

import edu.ubc.mirrors.ClassMirrorPrepareRequest;
import edu.ubc.mirrors.ConstructorMirrorEntryRequest;
import edu.ubc.mirrors.ConstructorMirrorExitRequest;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FieldMirrorGetRequest;
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

public class WrappingMirrorEventRequestManager implements MirrorEventRequestManager {

    private final WrappingVirtualMachine vm;
    private final MirrorEventRequestManager wrapped;
    
    public WrappingMirrorEventRequestManager(WrappingVirtualMachine vm, MirrorEventRequestManager wrapped) {
	this.vm = vm;
	this.wrapped = wrapped;
    }

    @Override
    public MirrorLocationRequest createLocationRequest(MirrorLocation location) {
        MirrorLocation wrappedLoc = ((WrappingMirrorLocation)location).getWrapped();
        return new WrappingMirrorLocationRequest(vm, wrapped.createLocationRequest(wrappedLoc));
    }
    
    @Override
    public List<MirrorLocationRequest> locationRequests() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public MethodMirrorEntryRequest createMethodMirrorEntryRequest() {
	return new WrappingMethodMirrorEntryRequest(vm, wrapped.createMethodMirrorEntryRequest());
    }

    @Override
    public List<MethodMirrorEntryRequest> methodMirrorEntryRequests() {
	throw new UnsupportedOperationException();
    }

    @Override
    public MethodMirrorExitRequest createMethodMirrorExitRequest() {
	return new WrappingMethodMirrorExitRequest(vm, wrapped.createMethodMirrorExitRequest());
    }

    @Override
    public List<MethodMirrorExitRequest> methodMirrorExitRequests() {
	throw new UnsupportedOperationException();
    }

    @Override
    public ConstructorMirrorEntryRequest createConstructorMirrorEntryRequest() {
	return new WrappingConstructorMirrorEntryRequest(vm, wrapped.createConstructorMirrorEntryRequest());
    }

    @Override
    public List<ConstructorMirrorEntryRequest> constructorMirrorEntryRequests() {
	throw new UnsupportedOperationException();
    }

    @Override
    public ConstructorMirrorExitRequest createConstructorMirrorExitRequest() {
	return new WrappingConstructorMirrorExitRequest(vm, wrapped.createConstructorMirrorExitRequest());
    }

    @Override
    public List<ConstructorMirrorExitRequest> constructorMirrorExitRequests() {
	throw new UnsupportedOperationException();
    }

    @Override
    public FieldMirrorGetRequest createFieldMirrorGetRequest(FieldMirror field) {
	FieldMirror unwrappedField = vm.unwrapFieldMirror(field);
	return new WrappingFieldMirrorGetRequest(vm, wrapped.createFieldMirrorGetRequest(unwrappedField));
    }

    @Override
    public List<FieldMirrorGetRequest> fieldMirrorGetRequests() {
	throw new UnsupportedOperationException();
    }

    @Override
    public FieldMirrorSetRequest createFieldMirrorSetRequest(FieldMirror field) {
        FieldMirror unwrappedField = vm.unwrapFieldMirror(field);
        return new WrappingFieldMirrorSetRequest(vm, wrapped.createFieldMirrorSetRequest(unwrappedField));
    }

    @Override
    public List<FieldMirrorSetRequest> fieldMirrorSetRequests() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassMirrorPrepareRequest createClassMirrorPrepareRequest() {
	return new WrappingClassMirrorPrepareRequest(vm, wrapped.createClassMirrorPrepareRequest());
    }

    @Override
    public List<ClassMirrorPrepareRequest> classMirrorPrepareRequests() {
	throw new UnsupportedOperationException();
    }

    @Override
    public ThreadMirrorDeathRequest createThreadMirrorDeathRequest() {
        return new WrappingThreadMirrorDeathRequest(vm, wrapped.createThreadMirrorDeathRequest());
    }
    
    @Override
    public ThreadMirrorStartRequest createThreadMirrorStartRequest() {
        return new WrappingThreadMirrorStartRequest(vm, wrapped.createThreadMirrorStartRequest());
    }
    
    @Override
    public void deleteMirrorEventRequest(MirrorEventRequest request) {
	throw new UnsupportedOperationException();
    }
    
    @Override
    public MethodMirrorHandlerRequest createMethodMirrorHandlerRequest(MirrorInvocationHandler handler) {
        return new WrappingMethodMirrorHandlerRequest(vm, 
                wrapped.createMethodMirrorHandlerRequest(vm.unwrapInvocationHandler(handler)));
    }
}
