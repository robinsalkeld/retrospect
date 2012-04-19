package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ThreadMirror;

public class WrappingThreadMirror extends WrappingInstanceMirror implements ThreadMirror {

    private final ThreadMirror wrappedThread;
    
    public WrappingThreadMirror(WrappingVirtualMachine vm, ThreadMirror wrappedThread) {
        super(vm, wrappedThread);
        this.wrappedThread = wrappedThread;
    }

    @Override
    public ObjectArrayMirror getStackTrace() {
        return (ObjectArrayMirror)vm.getWrappedMirror(wrappedThread.getStackTrace());
    }
}
