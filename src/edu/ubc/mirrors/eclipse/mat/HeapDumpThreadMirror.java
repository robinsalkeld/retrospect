package edu.ubc.mirrors.eclipse.mat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.IInstance;
import org.eclipse.mat.snapshot.model.IStackFrame;
import org.eclipse.mat.snapshot.model.IThreadStack;

import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ThreadMirror;
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
//            throw new RuntimeException(e);
            frames = new IStackFrame[0];
        }
        
        StackTraceElement[] trace = new StackTraceElement[frames.length];
        for (int i = 0; i < frames.length; i++) {
            String text = frames[i].getText();
            Matcher m = framePattern.matcher(text);
            if (m.matches()) {
                String className = m.group(1);
                String methodName = m.group(2);
                String fileName = m.group(3);
                String lineTxt = m.group(4);
                int line = (lineTxt != null ? Integer.parseInt(lineTxt) : -1);
                trace[i] = new StackTraceElement(className, methodName, fileName, line);
            } else {
                throw new RuntimeException("Unexpected frame text format: " + text);
            }
        }
        
        return (ObjectArrayMirror)NativeInstanceMirror.makeMirror(trace);
    }

}
