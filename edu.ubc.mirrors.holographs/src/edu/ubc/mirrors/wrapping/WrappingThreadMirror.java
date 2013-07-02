package edu.ubc.mirrors.wrapping;

import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.ThreadMirror;

public class WrappingThreadMirror extends WrappingInstanceMirror implements ThreadMirror {

    private final ThreadMirror wrappedThread;
    
    public WrappingThreadMirror(WrappingVirtualMachine vm, ThreadMirror wrappedThread) {
        super(vm, wrappedThread);
        this.wrappedThread = wrappedThread;
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
    public void interrupt() {
        wrappedThread.interrupt();
    }
}
