package edu.ubc.mirrors.jdi;

import java.util.List;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.mirages.Reflection;

public class JDIThreadMirror extends JDIInstanceMirror implements ThreadMirror {

    private final ThreadReference thread;
    
    public JDIThreadMirror(JDIVirtualMachineMirror vm, ThreadReference thread) {
        super(vm, thread);
        this.thread = thread;
    }

    @Override
    public ObjectArrayMirror getStackTrace() {
        List<StackFrame> stack;
        try {
            stack = thread.frames();
        } catch (IncompatibleThreadStateException e) {
            throw new RuntimeException(e);
        }
        ClassMirror steClass = vm.findBootstrapClassMirror(StackTraceElement.class.getName());
        int size = stack.size();
        ObjectArrayMirror result = (ObjectArrayMirror)steClass.newArray(size);
        for (int i = 0; i < size; i++) {
            result.set(i, stackTraceElementForLocation(stack.get(i).location()));
        }
        return result;
    }
    
    private InstanceMirror stackTraceElementForLocation(Location location) {
        String sourceName;
        try {
            sourceName = location.sourceName();
        } catch (AbsentInformationException e) {
            sourceName = "<Source Unknown>";
        }
        return Reflection.newStackTraceElement(vm, 
                location.method().declaringType().name(), 
                location.method().name(), 
                sourceName, 
                location.lineNumber());
    }
}
