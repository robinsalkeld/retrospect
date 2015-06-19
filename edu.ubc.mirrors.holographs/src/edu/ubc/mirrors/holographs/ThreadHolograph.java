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
package edu.ubc.mirrors.holographs;

import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.fieldmap.FieldMapFrameMirror;
import edu.ubc.mirrors.holograms.HologramClassGenerator;
import edu.ubc.mirrors.holograms.HologramClassLoader;
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
        Reflection.checkNull(wrappedThread);
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
        
        List<FrameMirror> result = new ArrayList<FrameMirror>(originalStack);
        // Need to deal with API mismatches here - we don't have the Classes on the stack for
        // arbitrary threads.
//        for (StackTraceElement ste : runningThread.getStackTrace()) {
//            String className = ste.getClassName();
//            if (className.startsWith("hologram")) {
//                String originalClassName = HologramClassGenerator.getOriginalBinaryClassName(className);
//                
//                // Unfortunately here we only have class names rather than actual classes
//                // so if any are ambiguous we're hooped.
//                List<ClassMirror> matchingClasses = vm.findAllClasses(originalClassName, false);
//                if (matchingClasses.isEmpty()) {
//                    // This indicates an error in the underlying VM
//                    throw new InternalError();
//                } else if (matchingClasses.size() > 1) {
//                    // This is just unfortunate but could happen
//                    throw new InternalError("Ambiguous class name on stack: " + className);
//                }
//                ClassMirror klass = matchingClasses.get(0);
//                MethodMirror method = klass.getDeclaredMethod(ste.getMethodName(), paramTypes)
//                
//                FrameMirror holographicFrame = new FieldMapFrameMirror(method, ste.getFileName(), ste.getLineNumber());
//                result.add(holographicFrame);
//            }
//        }
        
        return result;
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
        if (runningThread == null) {
            throw new IllegalStateException("Not in holograph execution.");
        }
        runningThread.interrupt(); 
    }
    
    public static boolean inMetalevel() {
        return metalevel.get().intValue() != 0;
    }
    
    public Thread getRunningThread() {
        return runningThread;
    }
    
    public String toString() {
        return getClass().getSimpleName() + " (" + Reflection.getThreadName(this) + ")";
    };
}
