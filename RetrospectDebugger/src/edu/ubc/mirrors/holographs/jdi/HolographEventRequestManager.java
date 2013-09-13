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
package edu.ubc.mirrors.holographs.jdi;

import java.util.List;

import com.sun.jdi.Field;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
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

public class HolographEventRequestManager extends Holograph implements EventRequestManager {

    private final EventRequestManager wrapped;
    
    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#accessWatchpointRequests()
     */
    public List<AccessWatchpointRequest> accessWatchpointRequests() {
        return wrapped.accessWatchpointRequests();
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#breakpointRequests()
     */
    public List<BreakpointRequest> breakpointRequests() {
        return wrapped.breakpointRequests();
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#classPrepareRequests()
     */
    public List<ClassPrepareRequest> classPrepareRequests() {
        return wrapped.classPrepareRequests();
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#classUnloadRequests()
     */
    public List<ClassUnloadRequest> classUnloadRequests() {
        return wrapped.classUnloadRequests();
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.request.EventRequestManager#createAccessWatchpointRequest(com.sun.jdi.Field)
     */
    public AccessWatchpointRequest createAccessWatchpointRequest(Field arg0) {
        return wrapped.createAccessWatchpointRequest(arg0);
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.request.EventRequestManager#createBreakpointRequest(com.sun.jdi.Location)
     */
    public BreakpointRequest createBreakpointRequest(Location arg0) {
        return wrapped.createBreakpointRequest(arg0);
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#createClassPrepareRequest()
     */
    public ClassPrepareRequest createClassPrepareRequest() {
        return new HolographClassPrepareRequest(vm, wrapped.createClassPrepareRequest());
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#createClassUnloadRequest()
     */
    public ClassUnloadRequest createClassUnloadRequest() {
        return wrapped.createClassUnloadRequest();
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @return
     * @see com.sun.jdi.request.EventRequestManager#createExceptionRequest(com.sun.jdi.ReferenceType, boolean, boolean)
     */
    public ExceptionRequest createExceptionRequest(ReferenceType arg0, boolean arg1, boolean arg2) {
        return wrapped.createExceptionRequest(vm.unwrapReferenceType(arg0), arg1, arg2);
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#createMethodEntryRequest()
     */
    public MethodEntryRequest createMethodEntryRequest() {
        return wrapped.createMethodEntryRequest();
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#createMethodExitRequest()
     */
    public MethodExitRequest createMethodExitRequest() {
        return wrapped.createMethodExitRequest();
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.request.EventRequestManager#createModificationWatchpointRequest(com.sun.jdi.Field)
     */
    public ModificationWatchpointRequest createModificationWatchpointRequest(
            Field arg0) {
        return wrapped.createModificationWatchpointRequest(arg0);
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#createMonitorContendedEnterRequest()
     */
    public MonitorContendedEnterRequest createMonitorContendedEnterRequest() {
        return wrapped.createMonitorContendedEnterRequest();
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#createMonitorContendedEnteredRequest()
     */
    public MonitorContendedEnteredRequest createMonitorContendedEnteredRequest() {
        return wrapped.createMonitorContendedEnteredRequest();
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#createMonitorWaitRequest()
     */
    public MonitorWaitRequest createMonitorWaitRequest() {
        return wrapped.createMonitorWaitRequest();
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#createMonitorWaitedRequest()
     */
    public MonitorWaitedRequest createMonitorWaitedRequest() {
        return wrapped.createMonitorWaitedRequest();
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @return
     * @see com.sun.jdi.request.EventRequestManager#createStepRequest(com.sun.jdi.ThreadReference, int, int)
     */
    public StepRequest createStepRequest(ThreadReference arg0, int arg1, int arg2) {
        return wrapped.createStepRequest(vm.unwrapThreadReference(arg0), arg1, arg2);
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#createThreadDeathRequest()
     */
    public ThreadDeathRequest createThreadDeathRequest() {
        return wrapped.createThreadDeathRequest();
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#createThreadStartRequest()
     */
    public ThreadStartRequest createThreadStartRequest() {
        return wrapped.createThreadStartRequest();
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#createVMDeathRequest()
     */
    public VMDeathRequest createVMDeathRequest() {
        return wrapped.createVMDeathRequest();
    }

    /**
     * 
     * @see com.sun.jdi.request.EventRequestManager#deleteAllBreakpoints()
     */
    public void deleteAllBreakpoints() {
        wrapped.deleteAllBreakpoints();
    }

    /**
     * @param arg0
     * @see com.sun.jdi.request.EventRequestManager#deleteEventRequest(com.sun.jdi.request.EventRequest)
     */
    public void deleteEventRequest(EventRequest arg0) {
        wrapped.deleteEventRequest(vm.unwrapEventRequest(arg0));
    }

    /**
     * @param arg0
     * @see com.sun.jdi.request.EventRequestManager#deleteEventRequests(java.util.List)
     */
    public void deleteEventRequests(List arg0) {
        wrapped.deleteEventRequests(arg0);
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#exceptionRequests()
     */
    public List<ExceptionRequest> exceptionRequests() {
        return wrapped.exceptionRequests();
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#methodEntryRequests()
     */
    public List<MethodEntryRequest> methodEntryRequests() {
        return wrapped.methodEntryRequests();
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#methodExitRequests()
     */
    public List<MethodExitRequest> methodExitRequests() {
        return wrapped.methodExitRequests();
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#modificationWatchpointRequests()
     */
    public List<ModificationWatchpointRequest> modificationWatchpointRequests() {
        return wrapped.modificationWatchpointRequests();
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#monitorContendedEnterRequests()
     */
    public List<MonitorContendedEnterRequest> monitorContendedEnterRequests() {
        return wrapped.monitorContendedEnterRequests();
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#monitorContendedEnteredRequests()
     */
    public List<MonitorContendedEnteredRequest> monitorContendedEnteredRequests() {
        return wrapped.monitorContendedEnteredRequests();
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#monitorWaitRequests()
     */
    public List<MonitorWaitRequest> monitorWaitRequests() {
        return wrapped.monitorWaitRequests();
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#monitorWaitedRequests()
     */
    public List<MonitorWaitedRequest> monitorWaitedRequests() {
        return wrapped.monitorWaitedRequests();
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#stepRequests()
     */
    public List<StepRequest> stepRequests() {
        return wrapped.stepRequests();
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#threadDeathRequests()
     */
    public List<ThreadDeathRequest> threadDeathRequests() {
        return wrapped.threadDeathRequests();
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#threadStartRequests()
     */
    public List<ThreadStartRequest> threadStartRequests() {
        return wrapped.threadStartRequests();
    }

    /**
     * @return
     * @see com.sun.jdi.Mirror#toString()
     */
    public String toString() {
        return wrapped.toString();
    }

    /**
     * @return
     * @see com.sun.jdi.request.EventRequestManager#vmDeathRequests()
     */
    public List<VMDeathRequest> vmDeathRequests() {
        return wrapped.vmDeathRequests();
    }

    public HolographEventRequestManager(JDIHolographVirtualMachine vm, EventRequestManager wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

}
