package edu.ubc.mirrors.eclipse.mat;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.ObjectReference;

import edu.ubc.mirrors.BoxingInstanceMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.StaticFieldValuesMirror;

public class HeapDumpClassStaticValues extends BoxingInstanceMirror implements StaticFieldValuesMirror {

    private final HeapDumpClassMirror forClassMirror;
    
    public HeapDumpClassStaticValues(HeapDumpClassMirror forClassMirror) {
        this.forClassMirror = forClassMirror;
    }
    
    @Override
    public ClassMirror getClassMirror() {
        return forClassMirror().getVM().findBootstrapClassMirror(Object.class.getName());
    }

    @Override
    public ClassMirror forClassMirror() {
        return forClassMirror;
    }
    
    @Override
    public int identityHashCode() {
        return 0;
    }
    
    @Override
    public Object getBoxedValue(FieldMirror field) throws IllegalAccessException {
        HeapDumpFieldMirror hdfm = (HeapDumpFieldMirror)field;
        return ((Field)hdfm.fieldDescriptor).getValue();
    }

    @Override
    public ObjectMirror get(FieldMirror field) throws IllegalAccessException {
        Object value = getBoxedValue(field);
        ObjectReference ref = (ObjectReference)value;
        if (ref == null) {
            return null;
        }
        try {
            return forClassMirror.getVM().makeMirror(ref.getObject());
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void setBoxedValue(FieldMirror field, Object o) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }
}
