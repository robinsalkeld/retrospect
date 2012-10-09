package edu.ubc.mirrors.eclipse.mat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.IInstance;
import org.eclipse.mat.snapshot.model.IStackFrame;
import org.eclipse.mat.snapshot.model.IThreadStack;

import com.sun.jdi.StackFrame;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FrameMirror;
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

    @Override
    public List<FrameMirror> getStackTrace() {
        
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
        
        List<FrameMirror> result = new ArrayList<FrameMirror>(frames.length);
        for (int i = 0; i < frames.length; i++) {
            String text = frames[i].getText();
            result.add(new HeapDumpFrameMirror(vm, text));
        }
        
        return result;
    }

}
