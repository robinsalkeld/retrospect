package edu.ubc.mirrors.jdi;

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

import edu.ubc.mirrors.FrameMirror;
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
    
    public void suspend() {
        thread.suspend();
    }
    
    public void interrupt() {
        thread.interrupt(); 
    }
}
