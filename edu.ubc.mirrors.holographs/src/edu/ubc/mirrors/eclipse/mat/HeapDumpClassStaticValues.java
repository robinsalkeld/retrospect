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

    private final HeapDumpVirtualMachineMirror vm;
    
    public HeapDumpClassStaticValues(HeapDumpVirtualMachineMirror vm) {
        this.vm = vm;
    }
    
    @Override
    public ClassMirror getClassMirror() {
        return vm.findBootstrapClassMirror(Object.class.getName());
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
            return vm.makeMirror(ref.getObject());
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void setBoxedValue(FieldMirror field, Object o) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }
}
