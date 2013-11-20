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
package edu.ubc.mirrors.eclipse.mat;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.IInstance;
import org.eclipse.mat.snapshot.model.IStackFrame;
import org.eclipse.mat.snapshot.model.IThreadStack;

import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ThreadMirror;

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

    @Override
    public List<InstanceMirror> getOwnedMonitors() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void interrupt() {
        throw new UnsupportedOperationException(); 
    }
}
