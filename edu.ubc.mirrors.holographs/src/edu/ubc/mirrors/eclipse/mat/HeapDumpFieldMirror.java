package edu.ubc.mirrors.eclipse.mat;

import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.FieldDescriptor;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;

public class HeapDumpFieldMirror implements FieldMirror {

    private final HeapDumpClassMirror declaringClass;
    final FieldDescriptor fieldDescriptor;
    
    public HeapDumpFieldMirror(HeapDumpClassMirror declaringClass, FieldDescriptor fieldDescriptor) {
        this.declaringClass = declaringClass;
        this.fieldDescriptor = fieldDescriptor;
    }
    
    boolean isStatic() {
        return fieldDescriptor instanceof Field;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HeapDumpFieldMirror)) {
            return false;
        }
        
        HeapDumpFieldMirror other = (HeapDumpFieldMirror)obj;
        return declaringClass.equals(other.declaringClass) && fieldDescriptor.equals(other.fieldDescriptor);
    }
    
    @Override
    public int hashCode() {
        return 7 * declaringClass.hashCode() * fieldDescriptor.hashCode();
    }
    
    @Override
    public ClassMirror getDeclaringClass() {
        return declaringClass;
    }

    @Override
    public String getName() {
        return fieldDescriptor.getName();
    }
    
    @Override
    public ClassMirror getType() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String getTypeName() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int getModifiers() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + fieldDescriptor.getVerboseSignature() + " " + fieldDescriptor.getName();
    }
}
