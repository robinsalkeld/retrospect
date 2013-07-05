package edu.ubc.mirrors.eclipse.mat;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IObjectArray;

import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;

public class HeapDumpObjectArrayMirror implements ObjectArrayMirror, HeapDumpObjectMirror {

    private final HeapDumpVirtualMachineMirror vm;
    private final IObjectArray array;
    
    public HeapDumpObjectArrayMirror(HeapDumpVirtualMachineMirror vm, IObjectArray array) {
        this.vm = vm;
        this.array = array;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        
        return ((HeapDumpObjectArrayMirror)obj).array.equals(array);
    }
    
    @Override
    public int hashCode() {
        return 11 * array.hashCode();
    }
    
    public HeapDumpClassMirror getClassMirror() {
        return (HeapDumpClassMirror)vm.makeMirror(array.getClazz());
    }

    @Override
    public int identityHashCode() {
        return vm.identityHashCode(array);
    }
    
    @Override
    public IObject getHeapDumpObject() {
        return array;
    }
    
    public int length() {
        return array.getLength();
    }

    public ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException {
        long address = ((IObjectArray)array).getReferenceArray()[index];
        if (address == 0) {
            return null;
        }
        try {
            IObject obj = array.getSnapshot().getObject(array.getSnapshot().mapAddressToId(address));
            return vm.makeMirror(obj);
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
    }

    public void set(int index, ObjectMirror o) throws ArrayIndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        return "HeapDumpObjectArrayMirror: " + array;
    }
}