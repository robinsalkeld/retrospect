package edu.ubc.mirrors.eclipse.mat;

import java.lang.ref.WeakReference;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IObjectArray;

import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;

public class HeapDumpObjectArrayMirror implements ObjectArrayMirror, HeapDumpObjectMirror {

    private final HeapDumpVirtualMachineMirror vm;
    private final IObjectArray array;
    
    private WeakReference<long[]> referenceArray;
    
    public HeapDumpObjectArrayMirror(HeapDumpVirtualMachineMirror vm, IObjectArray array) {
        this.vm = vm;
        this.array = array;
    }
    
    @Override
    public boolean equals(Object obj) {
        return HeapDumpVirtualMachineMirror.equalObjects(this, obj);
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
        long[] refArray = referenceArray != null ? referenceArray.get() : null;
        if (refArray == null) {
            refArray = ((IObjectArray)array).getReferenceArray();
            referenceArray = new WeakReference<long[]>(refArray);
        }
        
        long address = refArray[index];
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
