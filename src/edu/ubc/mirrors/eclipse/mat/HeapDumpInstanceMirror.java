package edu.ubc.mirrors.eclipse.mat;

import org.eclipse.mat.snapshot.model.IInstance;

import edu.ubc.mirrors.InstanceMirror;

public class HeapDumpInstanceMirror implements InstanceMirror, HeapDumpObjectMirror {

    protected final HeapDumpVirtualMachineMirror vm;
    protected final IInstance heapDumpObject;
    
    public HeapDumpInstanceMirror(HeapDumpVirtualMachineMirror vm, IInstance heapDumpObject) {
        this.vm = vm;
        this.heapDumpObject = heapDumpObject;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        
        return ((HeapDumpInstanceMirror)obj).heapDumpObject.equals(heapDumpObject);
    }
    
    @Override
    public int hashCode() {
        return 11 * heapDumpObject.hashCode();
    }
    
    public IInstance getHeapDumpObject() {
        return heapDumpObject;
    }
    
    public HeapDumpClassMirror getClassMirror() {
        return (HeapDumpClassMirror)vm.makeMirror(heapDumpObject.getClazz());
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + heapDumpObject;
    }
}
