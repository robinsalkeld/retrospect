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

import edu.ubc.mirrors.ClassMirrorPrepareRequest;
import edu.ubc.mirrors.ConstructorMirrorEntryRequest;
import edu.ubc.mirrors.ConstructorMirrorExitRequest;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FieldMirrorGetRequest;
import edu.ubc.mirrors.FieldMirrorSetRequest;
import edu.ubc.mirrors.MethodMirrorEntryRequest;
import edu.ubc.mirrors.MethodMirrorExitRequest;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.MirrorLocation;
import edu.ubc.mirrors.MirrorLocationRequest;
import edu.ubc.mirrors.ThreadMirrorDeathRequest;
import edu.ubc.mirrors.ThreadMirrorStartRequest;

public class HeapDumpEventRequestManager implements MirrorEventRequestManager {

    private final HeapDumpVirtualMachineMirror vm;
    
    public HeapDumpEventRequestManager(HeapDumpVirtualMachineMirror vm) {
        this.vm = vm;
    }

    @Override
    public MirrorLocationRequest createLocationRequest(MirrorLocation location) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public List<MirrorLocationRequest> locationRequests() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public MethodMirrorEntryRequest createMethodMirrorEntryRequest() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MethodMirrorEntryRequest> methodMirrorEntryRequests() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MethodMirrorExitRequest createMethodMirrorExitRequest() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MethodMirrorExitRequest> methodMirrorExitRequests() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConstructorMirrorEntryRequest createConstructorMirrorEntryRequest() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ConstructorMirrorEntryRequest> constructorMirrorEntryRequests() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConstructorMirrorExitRequest createConstructorMirrorExitRequest() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ConstructorMirrorExitRequest> constructorMirrorExitRequests() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FieldMirrorGetRequest createFieldMirrorGetRequest(FieldMirror field) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<FieldMirrorGetRequest> fieldMirrorGetRequests() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FieldMirrorSetRequest createFieldMirrorSetRequest(FieldMirror field) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<FieldMirrorSetRequest> fieldMirrorSetRequests() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ClassMirrorPrepareRequest createClassMirrorPrepareRequest() {
        return new HeapDumpClassMirrorPrepareRequest(vm);
    }

    @Override
    public List<ClassMirrorPrepareRequest> classMirrorPrepareRequests() {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        
    }
}
