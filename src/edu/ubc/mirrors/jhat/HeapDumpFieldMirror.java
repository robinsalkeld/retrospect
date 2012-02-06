package edu.ubc.mirrors.jhat;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.ObjectReference;

import edu.ubc.mirrors.BoxingFieldMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.mirages.MirageClassLoader;

public class HeapDumpFieldMirror extends BoxingFieldMirror {

    private final MirageClassLoader loader;
    private final Field field;
    
    public HeapDumpFieldMirror(MirageClassLoader loader, Field field) {
        this.loader = loader;
        this.field = field;
    }
    
    public Object get() throws IllegalAccessException {
        Object value = field.getValue();
        if (value instanceof ObjectReference) {
            ObjectReference ref = (ObjectReference)value;
            IObject object;
            try {
                object = ref.getObject();
            } catch (SnapshotException e) {
                throw new InternalError();
            }
            ObjectMirror<?> mirror = HeapDumpObjectMirror.makeMirror(loader, object);
            return loader.makeMirage(mirror);
        } else {
            return value;
        }
    }

    public void set(Object o) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }
}
