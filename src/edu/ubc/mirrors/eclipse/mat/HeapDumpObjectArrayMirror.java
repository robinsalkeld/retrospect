package edu.ubc.mirrors.eclipse.mat;

import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IObjectArray;
import org.eclipse.mat.snapshot.model.NamedReference;

import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;

public class HeapDumpObjectArrayMirror implements ObjectArrayMirror, HeapDumpObjectMirror {

    private final HeapDumpClassMirrorLoader loader;
    private final IObjectArray array;
    
    public HeapDumpObjectArrayMirror(HeapDumpClassMirrorLoader loader, IObjectArray array) {
        this.loader = loader;
        this.array = array;
    }
    
    public HeapDumpClassMirror getClassMirror() {
        return new HeapDumpClassMirror(loader, array.getClazz());
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
        return HeapDumpFieldMirror.getObjectWithErrorHandling(loader, new NamedReference(array.getSnapshot(), address, "[" + index + "]"));
    }

    public void set(int index, ObjectMirror o) throws ArrayIndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }
}
