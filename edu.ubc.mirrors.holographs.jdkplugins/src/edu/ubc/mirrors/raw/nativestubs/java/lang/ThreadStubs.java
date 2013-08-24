package edu.ubc.mirrors.raw.nativestubs.java.lang;

import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.InstanceMirror;
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
        return thread.getStackTrace() != null;
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
                element.set(stackTraceElementClass.getDeclaredField("declaringClass"), Reflection.makeString(vm, frame.declaringClass().getClassName()));
                element.set(stackTraceElementClass.getDeclaredField("methodName"), Reflection.makeString(vm, frame.method().getName()));
                element.set(stackTraceElementClass.getDeclaredField("fileName"), Reflection.makeString(vm, frame.fileName()));
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
    
}