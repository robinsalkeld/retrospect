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

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.MonitorInfo;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;

public class MirrorsThreadReference extends MirrorsInstanceReference implements ThreadReference {

    protected final ThreadMirror wrapped;
    
    public MirrorsThreadReference(MirrorsVirtualMachine vm, ThreadMirror wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    @Override
    public ObjectReference currentContendedMonitor() throws IncompatibleThreadStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forceEarlyReturn(Value value) throws InvalidTypeException,
            ClassNotLoadedException, IncompatibleThreadStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public StackFrame frame(int index) throws IncompatibleThreadStateException {
        return new MirrorsStackFrame(vm, this, wrapped.getStackTrace().get(index));
    }

    @Override
    public int frameCount() throws IncompatibleThreadStateException {
        return wrapped.getStackTrace().size();
    }

    @Override
    public List<StackFrame> frames() throws IncompatibleThreadStateException {
        List<StackFrame> result = new ArrayList<StackFrame>();
        for (FrameMirror frame : wrapped.getStackTrace()) {
            result.add(new MirrorsStackFrame(vm, this, frame));
        }
        return result;
    }

    @Override
    public List<StackFrame> frames(int start, int length) throws IncompatibleThreadStateException {
        return frames().subList(start, start + length);
    }

    @Override
    public void interrupt() {
        wrapped.interrupt();
    }

    @Override
    public boolean isAtBreakpoint() {
        return false;
    }

    @Override
    public boolean isSuspended() {
        // TODO-RS
        return true;
    }

    @Override
    public String name() {
        return Reflection.getThreadName(wrapped);
    }

    @Override
    public List<ObjectReference> ownedMonitors() throws IncompatibleThreadStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MonitorInfo> ownedMonitorsAndFrames()
            throws IncompatibleThreadStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void popFrames(StackFrame arg0)
            throws IncompatibleThreadStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resume() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int status() {
        return THREAD_STATUS_RUNNING;
    }

    @Override
    public void stop(ObjectReference arg0) throws InvalidTypeException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void suspend() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int suspendCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ThreadGroupReference threadGroup() {
        return (ThreadGroupReference)vm.wrapMirror(readField(vm.vm.findBootstrapClassMirror(Thread.class.getName()), "group"));
    }
}
