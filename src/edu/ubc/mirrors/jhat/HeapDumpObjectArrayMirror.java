package edu.ubc.mirrors.jhat;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IObjectArray;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;

public class HeapDumpObjectArrayMirror implements ObjectArrayMirror {

    private final HeapDumpClassMirrorLoader loader;
    private final IObjectArray array;
    
    public HeapDumpObjectArrayMirror(HeapDumpClassMirrorLoader loader, IObjectArray array) {
        this.loader = loader;
        this.array = array;
    }
    
    public ClassMirror getClassMirror() {
        return new HeapDumpClassMirror(loader, array.getClazz());
    }

    public int length() {
        return array.getLength();
    }

    public ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException {
        long address = ((IObjectArray)array).getReferenceArray()[index];
        IObject object;
        try {
            object = array.getSnapshot().getObject(array.getSnapshot().mapAddressToId(address));
        } catch (SnapshotException e) {
            throw new InternalError();
        }
        return HeapDumpObjectMirror.makeMirror(loader, object);
    }

    public void set(int index, ObjectMirror o) throws ArrayIndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }
}
