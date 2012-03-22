package edu.ubc.mirrors.eclipse.mat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.IInstance;
import org.eclipse.mat.snapshot.model.IStackFrame;
import org.eclipse.mat.snapshot.model.IThreadStack;

import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.raw.NativeObjectMirror;

public class HeapDumpThreadMirror extends HeapDumpInstanceMirror implements ThreadMirror {

    public HeapDumpThreadMirror(HeapDumpClassMirrorLoader loader, IInstance heapDumpObject) {
        super(loader, heapDumpObject);
    }

    private static final Pattern framePattern = Pattern.compile("at (.*)\\.(.*)\\(.*\\).* \\((?:(.*):(\\d*)|(.*))\\)");
    
    @Override
    public ObjectArrayMirror getStackTrace() {
        IThreadStack stack;
        try {
            stack = heapDumpObject.getSnapshot().getThreadStack(heapDumpObject.getObjectId());
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
        
        if (stack == null) {
            return null;
        }
        
        IStackFrame[] frames = stack.getStackFrames();
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
        
        return (ObjectArrayMirror)NativeObjectMirror.makeMirror(trace);
    }

}
