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
package edu.ubc.mirrors.raw.nativestubs.java.lang;

import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class ThreadStubs extends NativeStubs {
    
    public ThreadStubs(ClassHolograph klass) {
	super(klass);
    }

    @StubMethod
    public ThreadMirror currentThread() {
        return ThreadHolograph.currentThreadMirror();
    }
    
    @StubMethod
    public boolean isAlive(ThreadMirror thread) {
        // TODO-RS: This may need more precision in the API
        return !thread.getStackTrace().isEmpty();
    }
    
    @StubMethod
    public ObjectArrayMirror getStackTrace(ThreadMirror thread) {
        VirtualMachineMirror vm = getVM();
        
        List<FrameMirror> trace = thread.getStackTrace();
        int size = trace.size();
        ClassMirror stackTraceElementClass = vm.findBootstrapClassMirror(StackTraceElement.class.getName());
        ObjectArrayMirror result = (ObjectArrayMirror)stackTraceElementClass.newArray(size);
        for (int i = 0; i < size; i++) {
            FrameMirror frame = trace.get(i);
            InstanceMirror element = stackTraceElementClass.newRawInstance();
            try {
                element.set(stackTraceElementClass.getDeclaredField("declaringClass"), vm.makeString(frame.declaringClass().getClassName()));
                element.set(stackTraceElementClass.getDeclaredField("methodName"), vm.makeString(frame.methodName()));
                element.set(stackTraceElementClass.getDeclaredField("fileName"), vm.makeString(frame.fileName()));
                element.setInt(stackTraceElementClass.getDeclaredField("lineNumber"), frame.lineNumber());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
	    result.set(i, element);
	}
        return result;
    }
    
    @StubMethod
    public boolean isInterrupted(ThreadMirror thread, boolean ClearInterrupted) {
        // TODO-RS: How to implement this for a heap dump?
        return false;
    }
    
    @StubMethod
    public void setPriority0(ThreadMirror threadMirror, int newPriority) {
//        ThreadHolograph threadHolograph = (ThreadHolograph)threadMirror;
//        Thread thread = threadHolograph.getRunningThread();
//        thread.setPriority(newPriority);
    }
    
    @StubMethod
    public void start0(final ThreadMirror threadMirror) {
        ClassMirror threadClass = getVM().findBootstrapClassMirror(Thread.class.getName());
        final MethodMirror runMethod;
        final MethodMirror exitMethod;
        try {
            runMethod = threadClass.getDeclaredMethod("run");
            exitMethod = threadClass.getDeclaredMethod("exit");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    runMethod.invoke(threadMirror, threadMirror);
                    exitMethod.invoke(threadMirror, threadMirror);
                } catch (MirrorInvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                
                getVM().threadExited(threadMirror);
            }
        };
        thread.start();
        getVM().threadStarted(threadMirror);
    }
    
}
