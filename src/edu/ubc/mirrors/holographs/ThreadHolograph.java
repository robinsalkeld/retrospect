package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.wrapping.WrappingThreadMirror;
import edu.ubc.mirrors.wrapping.WrappingVirtualMachine;

public class ThreadHolograph extends WrappingThreadMirror {

    private Thread runningThread = null;
    private static ThreadLocal<ThreadMirror> currentThreadMirror = new ThreadLocal<ThreadMirror>();
    
    public ThreadHolograph(WrappingVirtualMachine vm, ThreadMirror wrappedThread) {
        super(vm, wrappedThread);
    }

    public void enterHologramExecution() {
        if (runningThread != null) {
            throw new IllegalStateException();
        }
        runningThread = Thread.currentThread();
        currentThreadMirror.set(this);
    }
    
    public static ThreadMirror currentThreadMirror() {
        ThreadMirror result = currentThreadMirror.get();
        if (result == null) {
            throw new IllegalStateException("Not in holograph execution.");
        }
        return result;
    }
    
    public void exitHologramExecution() {
        if (runningThread != Thread.currentThread()) {
            throw new IllegalStateException();
        }
        runningThread = null;
        currentThreadMirror.set(null);
    }
    
    @Override
    public ObjectArrayMirror getStackTrace() {
        ObjectArrayMirror originalStack = super.getStackTrace();
        
        if (runningThread == null) {
            return originalStack;
        }
        
        // TODO-RS: Need to remove non-MirageClassLoader frames
//        StackTraceElement[] holographicTrace = runningThread.getStackTrace();
        
        
        return originalStack;
    }
}
