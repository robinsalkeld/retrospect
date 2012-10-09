package edu.ubc.mirrors.wrapping;

import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ThreadMirror;

public class WrappingThreadMirror extends WrappingInstanceMirror implements ThreadMirror {

    private final ThreadMirror wrappedThread;
    
    public WrappingThreadMirror(WrappingVirtualMachine vm, ThreadMirror wrappedThread) {
        super(vm, wrappedThread);
        this.wrappedThread = wrappedThread;
    }

    @Override
    public List<FrameMirror> getStackTrace() {
	List<FrameMirror> result = new ArrayList<FrameMirror>();
	for (FrameMirror frame : wrappedThread.getStackTrace()) {
	    result.add(new WrappingFrameMirror(vm, frame));
	}
        return result;
    }
}
