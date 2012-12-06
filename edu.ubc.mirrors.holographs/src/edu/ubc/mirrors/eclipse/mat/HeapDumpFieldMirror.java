package edu.ubc.mirrors.eclipse.mat;

import java.util.Collections;
import java.util.Set;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.IPathsFromGCRootsComputer;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.FieldDescriptor;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IInstance;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IObjectArray;
import org.eclipse.mat.snapshot.model.NamedReference;
import org.eclipse.mat.snapshot.model.ObjectReference;

import edu.ubc.mirrors.BoxingFieldMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;

public class HeapDumpFieldMirror implements FieldMirror {

    private final HeapDumpClassMirror declaringClass;
    final FieldDescriptor fieldDescriptor;
    
    private final HeapDumpVirtualMachineMirror vm;
    
    public HeapDumpFieldMirror(HeapDumpVirtualMachineMirror vm, HeapDumpClassMirror declaringClass, FieldDescriptor fieldDescriptor) {
        this.vm = vm;
        this.declaringClass = declaringClass;
        this.fieldDescriptor = fieldDescriptor;
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
