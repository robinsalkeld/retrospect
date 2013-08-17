package edu.ubc.mirrors.eclipse.mat;

import java.util.AbstractList;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.IInstance;

import edu.ubc.mirrors.ObjectMirror;

public class HeapDumpLazyInstanceList extends AbstractList<ObjectMirror> {

    public HeapDumpLazyInstanceList(HeapDumpVirtualMachineMirror vm, int[] objectIDs) {
        this.vm = vm;
        this.objectIDs = objectIDs;
    }

    private final HeapDumpVirtualMachineMirror vm;
    private final int[] objectIDs;

    @Override
    public ObjectMirror get(int index) {
        try {
            IInstance object = (IInstance)vm.getSnapshot().getObject(objectIDs[index]);
            // Don't call vm.makeMirror() to avoid caching.
            return new HeapDumpInstanceMirror(vm, object);
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int size() {
        return objectIDs.length;
    }
    
}
