package edu.ubc.mirrors.holographs;

import java.util.List;

import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.wrapping.WrappingThreadMirror;
import edu.ubc.mirrors.wrapping.WrappingVirtualMachine;

public class ThreadHolograph extends InstanceHolograph implements ThreadMirror {

    private final ThreadMirror wrappedThread;
    private Thread runningThread = null;
    private int runningThreadCount = 0;
    public static ThreadLocal<ThreadHolograph> currentThreadMirror = new ThreadLocal<ThreadHolograph>();
    
    public static ThreadLocal<Integer> metalevel = new ThreadLocal<Integer>() {
        protected Integer initialValue() {
            return 0;
        };
    };
    
    public static void raiseMetalevel() {
        metalevel.set(metalevel.get() + 1);
    }
    
    public static void lowerMetalevel() {
        metalevel.set(metalevel.get() - 1);
    }
    
    public ThreadHolograph(VirtualMachineHolograph vm, ThreadMirror wrappedThread) {
        super(vm, wrappedThread);
        this.wrappedThread = wrappedThread;
    }

    public synchronized void enterHologramExecution() {
        if (runningThread != null && runningThread != Thread.currentThread()) {
            throw new IllegalStateException();
        }
        ThreadHolograph thisThreadsMirror = currentThreadMirror.get();
        if (thisThreadsMirror != null && thisThreadsMirror != this) {
            throw new IllegalStateException();
        }
        
        runningThread = Thread.currentThread();
        runningThreadCount++;
        currentThreadMirror.set(this);
    }
    
    public static ThreadHolograph currentThreadMirrorNoError() {
        return currentThreadMirror.get();
    }
    
    public synchronized static ThreadHolograph currentThreadMirror() {
	ThreadHolograph result = currentThreadMirrorNoError();
        if (result == null) {
            throw new IllegalStateException("Not in holograph execution.");
        }
        return result;
    }
    
    public synchronized void exitHologramExecution() {
	if (runningThreadCount <= 0 || runningThread != Thread.currentThread()) {
            throw new IllegalStateException();
        }
        if (--runningThreadCount == 0) {
            runningThread = null;
            currentThreadMirror.set(null);
        }
    }
    
    @Override
    public List<FrameMirror> getStackTrace() {
	List<FrameMirror> originalStack = WrappingThreadMirror.getWrappedStackTrace(vm, wrappedThread);
        
        if (runningThread == null) {
            return originalStack;
        }
        
        // TODO-RS: Need to append native frames, removing non-MirageClassLoader frames.
        // See also ReflectionStubs#getCallerClass
//        StackTraceElement[] holographicTrace = runningThread.getStackTrace();
        
        return originalStack;
    }

    @Override
    public void interrupt() {
        if (runningThread == null) {
            throw new IllegalStateException("Not in holograph execution.");
        }
        runningThread.interrupt(); 
    }
    
    public static boolean inMetalevel() {
        return metalevel.get().intValue() != 0;
    }
    
    public String toString() {
        return getClass().getSimpleName() + " (" + Reflection.getThreadName(this) + ")";
    };
}