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

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ThreadMirror;

public class JDIThreadMirror extends JDIInstanceMirror implements ThreadMirror {

    protected final ThreadReference thread;
    
    public JDIThreadMirror(JDIVirtualMachineMirror vm, ThreadReference thread) {
        super(vm, thread);
        this.thread = thread;
    }

    @Override
    public List<FrameMirror> getStackTrace() {
	List<StackFrame> stack;
        try {
            stack = thread.frames();
        } catch (IncompatibleThreadStateException e) {
            throw new RuntimeException(e);
        }
        List<FrameMirror> result = new ArrayList<FrameMirror>(stack.size());
        for (StackFrame frame : stack) {
            result.add(new JDIFrameMirror(vm, frame));
        }
        return result;
    }
    
    @Override
    public List<InstanceMirror> getOwnedMonitors() {
        List<InstanceMirror> result = new ArrayList<InstanceMirror>();
        try {
            for (ObjectReference monitor : thread.ownedMonitors()) {
                result.add((InstanceMirror)vm.makeMirror(monitor));
            }
        } catch (IncompatibleThreadStateException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    
    public void suspend() {
        thread.suspend();
    }
    
    public void interrupt() {
        thread.interrupt(); 
    }
}
