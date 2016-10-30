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
package edu.ubc.mirrors.eclipse.mat;

import java.util.List;

import edu.ubc.mirrors.AdviceMirrorHandlerRequest;
import edu.ubc.mirrors.ClassMirrorPrepareRequest;
import edu.ubc.mirrors.ConstructorMirrorEntryRequest;
import edu.ubc.mirrors.ConstructorMirrorExitRequest;
import edu.ubc.mirrors.ConstructorMirrorHandlerRequest;
import edu.ubc.mirrors.FieldMirrorGetHandlerRequest;
import edu.ubc.mirrors.FieldMirrorGetRequest;
import edu.ubc.mirrors.FieldMirrorSetHandlerRequest;
import edu.ubc.mirrors.FieldMirrorSetRequest;
import edu.ubc.mirrors.MethodMirrorEntryRequest;
import edu.ubc.mirrors.MethodMirrorExitRequest;
import edu.ubc.mirrors.MethodMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.MirrorLocation;
import edu.ubc.mirrors.MirrorLocationRequest;
import edu.ubc.mirrors.ThreadMirrorDeathRequest;
import edu.ubc.mirrors.ThreadMirrorStartRequest;
import edu.ubc.mirrors.VMMirrorDeathRequest;

public class HeapDumpEventRequestManager implements MirrorEventRequestManager {

    private final HeapDumpVirtualMachineMirror vm;
    
    public HeapDumpEventRequestManager(HeapDumpVirtualMachineMirror vm) {
        this.vm = vm;
    }

    @Override
    public MirrorLocationRequest createLocationRequest(MirrorLocation location) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public List<MirrorLocationRequest> locationRequests() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public MethodMirrorEntryRequest createMethodMirrorEntryRequest() {
        return new HeapDumpMethodMirrorEntryRequest(vm);
    }

    @Override
    public List<MethodMirrorEntryRequest> methodMirrorEntryRequests() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MethodMirrorExitRequest createMethodMirrorExitRequest() {
        return new HeapDumpMethodMirrorExitRequest(vm);
    }

    @Override
    public List<MethodMirrorExitRequest> methodMirrorExitRequests() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConstructorMirrorEntryRequest createConstructorMirrorEntryRequest() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ConstructorMirrorEntryRequest> constructorMirrorEntryRequests() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConstructorMirrorExitRequest createConstructorMirrorExitRequest() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ConstructorMirrorExitRequest> constructorMirrorExitRequests() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldMirrorGetRequest createFieldMirrorGetRequest(String declaringClass, String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<FieldMirrorGetRequest> fieldMirrorGetRequests() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldMirrorSetRequest createFieldMirrorSetRequest(String declaringClass, String fieldName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<FieldMirrorSetRequest> fieldMirrorSetRequests() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassMirrorPrepareRequest createClassMirrorPrepareRequest() {
        return new HeapDumpClassMirrorPrepareRequest(vm);
    }

    @Override
    public List<ClassMirrorPrepareRequest> classMirrorPrepareRequests() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ThreadMirrorStartRequest createThreadMirrorStartRequest() {
        return new HeapDumpThreadMirrorStartRequest(vm);
    }
    
    @Override
    public ThreadMirrorDeathRequest createThreadMirrorDeathRequest() {
        return new HeapDumpThreadMirrorDeathRequest(vm);
    }
    
    @Override
    public void deleteMirrorEventRequest(MirrorEventRequest request) {
        throw new UnsupportedOperationException();
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
    public FieldMirrorSetHandlerRequest createFieldMirrorSetHandlerRequest(String declaringClass, String fieldName) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public FieldMirrorGetHandlerRequest createFieldMirrorGetHandlerRequest(String declaringClass, String fieldName) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public AdviceMirrorHandlerRequest createAdviceMirrorHandlerRequest() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public VMMirrorDeathRequest createVMMirrorDeathRequest() {
        return new HeapDumpVMMirrorDeathRequest(vm);
    }
}
