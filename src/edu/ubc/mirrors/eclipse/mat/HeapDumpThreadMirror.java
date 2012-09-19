package edu.ubc.mirrors.eclipse.mat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.IInstance;
import org.eclipse.mat.snapshot.model.IStackFrame;
import org.eclipse.mat.snapshot.model.IThreadStack;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.fieldmap.DirectArrayMirror;
import edu.ubc.mirrors.fieldmap.FieldMapMirror;
import edu.ubc.mirrors.fieldmap.FieldMapStackTraceElementMirror;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.ArrayClassMirror;
import edu.ubc.mirrors.raw.NativeInstanceMirror;

public class HeapDumpThreadMirror extends HeapDumpInstanceMirror implements ThreadMirror {

    public HeapDumpThreadMirror(HeapDumpVirtualMachineMirror vm, IInstance heapDumpObject) {
        super(vm, heapDumpObject);
    }

    private static final Pattern framePattern = Pattern.compile("at (.*)\\.(.*)\\(.*\\).* \\((?:(.*):(\\d*)|(.*))\\)");
    
    @Override
    public ObjectArrayMirror getStackTrace() {
        
        IStackFrame[] frames;
        try {
            IThreadStack stack = heapDumpObject.getSnapshot().getThreadStack(heapDumpObject.getObjectId());
            if (stack == null) {
                return null;
            }
            
            frames = stack.getStackFrames();
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
        
        ClassMirror stackTraceElementClass = vm.findBootstrapClassMirror(StackTraceElement.class.getName());
        ClassMirror stackTraceArrayClass = new ArrayClassMirror(1, stackTraceElementClass);
        ObjectArrayMirror trace = new DirectArrayMirror(stackTraceArrayClass, frames.length);
        for (int i = 0; i < frames.length; i++) {
            String text = frames[i].getText();
            Matcher m = framePattern.matcher(text);
            if (m.matches()) {
                String className = m.group(1);
                String methodName = m.group(2);
                String fileName = m.group(3);
                String lineTxt = m.group(4);
                int line = (lineTxt != null ? Integer.parseInt(lineTxt) : -1);
                
                FieldMapStackTraceElementMirror ste = new FieldMapStackTraceElementMirror(vm, className, methodName, fileName, line);
                trace.set(i, ste);
            } else {
                throw new RuntimeException("Unexpected frame text format: " + text);
            }
        }
        
        return trace;
    }

}
