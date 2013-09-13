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
package edu.ubc.mirrors.asjdi;

import java.util.Collections;
import java.util.List;

import com.sun.jdi.Field;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.ClassUnloadRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import com.sun.jdi.request.ModificationWatchpointRequest;
import com.sun.jdi.request.MonitorContendedEnterRequest;
import com.sun.jdi.request.MonitorContendedEnteredRequest;
import com.sun.jdi.request.MonitorWaitRequest;
import com.sun.jdi.request.MonitorWaitedRequest;
import com.sun.jdi.request.StepRequest;
import com.sun.jdi.request.ThreadDeathRequest;
import com.sun.jdi.request.ThreadStartRequest;
import com.sun.jdi.request.VMDeathRequest;

import edu.ubc.mirrors.MirrorEventRequestManager;

public class MirrorsEventRequestManager extends MirrorsMirror implements EventRequestManager {

    private final MirrorEventRequestManager wrapped;
    
    public MirrorsEventRequestManager(MirrorsVirtualMachine vm, MirrorEventRequestManager wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    @Override
    public List<AccessWatchpointRequest> accessWatchpointRequests() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<BreakpointRequest> breakpointRequests() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ClassPrepareRequest> classPrepareRequests() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ClassUnloadRequest> classUnloadRequests() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AccessWatchpointRequest createAccessWatchpointRequest(Field arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BreakpointRequest createBreakpointRequest(Location arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassPrepareRequest createClassPrepareRequest() {
        return new MirrorsClassPrepareRequest(vm, wrapped.createClassMirrorPrepareRequest());
    }

    @Override
    public ClassUnloadRequest createClassUnloadRequest() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExceptionRequest createExceptionRequest(ReferenceType arg0, boolean arg1, boolean arg2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MethodEntryRequest createMethodEntryRequest() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MethodExitRequest createMethodExitRequest() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ModificationWatchpointRequest createModificationWatchpointRequest(Field arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MonitorContendedEnterRequest createMonitorContendedEnterRequest() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MonitorContendedEnteredRequest createMonitorContendedEnteredRequest() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MonitorWaitRequest createMonitorWaitRequest() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MonitorWaitedRequest createMonitorWaitedRequest() {
        throw new UnsupportedOperationException();
    }

    @Override
    public StepRequest createStepRequest(ThreadReference arg0, int arg1, int arg2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ThreadDeathRequest createThreadDeathRequest() {
        return new MirrorsThreadDeathRequest(vm, wrapped.createThreadMirrorDeathRequest());
    }

    @Override
    public ThreadStartRequest createThreadStartRequest() {
        return new MirrorsThreadStartRequest(vm);
    }

    @Override
    public VMDeathRequest createVMDeathRequest() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllBreakpoints() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteEventRequest(EventRequest request) {
        wrapped.deleteMirrorEventRequest(((MirrorsEventRequest)request).wrapped);
    }

    @Override
    public void deleteEventRequests(List<? extends EventRequest> requests) {
        for (Object request : requests) {
            deleteEventRequest((EventRequest)request);
        }
    }

    @Override
    public List<ExceptionRequest> exceptionRequests() {
        return Collections.emptyList();
    }

    @Override
    public List<MethodEntryRequest> methodEntryRequests() {
        return Collections.emptyList();
    }

    @Override
    public List<MethodExitRequest> methodExitRequests() {
        return Collections.emptyList();
    }

    @Override
    public List<ModificationWatchpointRequest> modificationWatchpointRequests() {
        return Collections.emptyList();
    }

    @Override
    public List<MonitorContendedEnterRequest> monitorContendedEnterRequests() {
        return Collections.emptyList();
    }

    @Override
    public List<MonitorContendedEnteredRequest> monitorContendedEnteredRequests() {
        return Collections.emptyList();
    }

    @Override
    public List<MonitorWaitRequest> monitorWaitRequests() {
        return Collections.emptyList();
    }

    @Override
    public List<MonitorWaitedRequest> monitorWaitedRequests() {
        return Collections.emptyList();
    }

    @Override
    public List<StepRequest> stepRequests() {
        return Collections.emptyList();
    }

    @Override
    public List<ThreadDeathRequest> threadDeathRequests() {
        return Collections.emptyList();
    }

    @Override
    public List<ThreadStartRequest> threadStartRequests() {
        return Collections.emptyList();
    }

    @Override
    public List<VMDeathRequest> vmDeathRequests() {
        return Collections.emptyList();
    }

   

}
