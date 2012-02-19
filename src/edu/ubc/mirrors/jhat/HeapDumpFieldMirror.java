package edu.ubc.mirrors.jhat;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.ObjectReference;

import edu.ubc.mirrors.BoxingFieldMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.mirages.MirageClassLoader;

public class HeapDumpFieldMirror extends BoxingFieldMirror {

    private final HeapDumpClassMirrorLoader loader;
    private final Field field;
    
    public HeapDumpFieldMirror(HeapDumpClassMirrorLoader loader, Field field) {
        this.loader = loader;
        this.field = field;
    }
    
    @Override
    public Object getBoxedValue() throws IllegalAccessException {
        return field.getValue();
    }
    
    public ObjectMirror get() throws IllegalAccessException {
        Object value = field.getValue();
        ObjectReference ref = (ObjectReference)value;
        IObject object;
        try {
            object = ref.getObject();
        } catch (SnapshotException e) {
            throw new InternalError();
        }
        return HeapDumpObjectMirror.makeMirror(loader, object);
    }

    public void set(ObjectMirror o) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void setBoxedValue(Object o) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }
    
}
