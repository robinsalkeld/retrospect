package edu.ubc.mirrors.eclipse.mat;

import org.eclipse.mat.snapshot.model.Field;

import edu.ubc.mirrors.BoxingInstanceMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;

public class HeapDumpClassStaticValues extends BoxingInstanceMirror {

    public static final HeapDumpClassStaticValues INSTANCE = new HeapDumpClassStaticValues();

    @Override
    public ClassMirror getClassMirror() {
        return null;
    }

    @Override
    public Object getBoxedValue(FieldMirror field) throws IllegalAccessException {
        HeapDumpFieldMirror hdfm = (HeapDumpFieldMirror)field;
        return ((Field)hdfm.fieldDescriptor).getValue();
    }

    @Override
    public void setBoxedValue(FieldMirror field, Object o) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }
}
