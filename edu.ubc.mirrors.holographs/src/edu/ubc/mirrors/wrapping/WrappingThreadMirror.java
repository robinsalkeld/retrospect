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

import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;

public class WrappingThreadMirror extends WrappingInstanceMirror implements ThreadMirror {

    protected final ThreadMirror wrappedThread;
    
    public WrappingThreadMirror(WrappingVirtualMachine vm, ThreadMirror wrappedThread) {
        super(vm, wrappedThread);
        this.wrappedThread = wrappedThread;
        Reflection.checkNull(wrappedThread);
    }

    public static List<FrameMirror> getWrappedStackTrace(WrappingVirtualMachine vm, ThreadMirror wrappedThread) {
        List<FrameMirror> result = new ArrayList<FrameMirror>();
        for (FrameMirror frame : wrappedThread.getStackTrace()) {
            result.add(vm.wrapFrameMirror(vm, frame));
        }
        return result;
    }
    
    @Override
    public List<FrameMirror> getStackTrace() {
	return getWrappedStackTrace(vm, wrappedThread);
    }
    
    @Override
    public InstanceMirror getContendedMonitor() {
        return (InstanceMirror)(vm.getWrappedMirror(wrappedThread.getContendedMonitor()));
    }
    
    @Override
    public List<InstanceMirror> getOwnedMonitors() {
        List<InstanceMirror> result = new ArrayList<InstanceMirror>();
        for (InstanceMirror monitor : wrappedThread.getOwnedMonitors()) {
            result.add((InstanceMirror)vm.getWrappedMirror(monitor));
        }
        return result;
    }
    
    @Override
    public void interrupt() {
        wrappedThread.interrupt();
    }
}
