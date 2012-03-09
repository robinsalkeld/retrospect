package edu.ubc.mirrors.eclipse.mat;

import org.eclipse.mat.snapshot.model.IObject;

import edu.ubc.mirrors.ObjectMirror;

public interface HeapDumpObjectMirror extends ObjectMirror {

    public IObject getHeapDumpObject();
    
    public HeapDumpClassMirror getClassMirror();
}
