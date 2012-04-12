package edu.ubc.mirrors.eclipse.mat;

import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IObjectArray;
import org.eclipse.mat.snapshot.model.NamedReference;

import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;

public class HeapDumpObjectArrayMirror implements ObjectArrayMirror, HeapDumpObjectMirror {

    private final HeapDumpVirtualMachineMirror vm;
    private final IObjectArray array;
    
    public HeapDumpObjectArrayMirror(HeapDumpVirtualMachineMirror vm, IObjectArray array) {
        this.vm = vm;
        this.array = array;
    }
    
    public HeapDumpClassMirror getClassMirror() {
        return (HeapDumpClassMirror)vm.makeMirror(array.getClazz());
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
        return HeapDumpFieldMirror.getObjectWithErrorHandling(vm, new NamedReference(array.getSnapshot(), address, "[" + index + "]"));
    }

    public void set(int index, ObjectMirror o) throws ArrayIndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        return "HeapDumpObjectArrayMirror: " + array;
    }
}
