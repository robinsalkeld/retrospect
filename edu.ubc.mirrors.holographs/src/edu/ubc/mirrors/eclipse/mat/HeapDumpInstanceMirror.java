package edu.ubc.mirrors.eclipse.mat;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.IInstance;
import org.eclipse.mat.snapshot.model.ObjectReference;

import edu.ubc.mirrors.BoxingInstanceMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.ObjectMirror;

public class HeapDumpInstanceMirror extends BoxingInstanceMirror implements HeapDumpObjectMirror {

    protected final HeapDumpVirtualMachineMirror vm;
    protected final IInstance heapDumpObject;
    
    public HeapDumpInstanceMirror(HeapDumpVirtualMachineMirror vm, IInstance heapDumpObject) {
        this.vm = vm;
        this.heapDumpObject = heapDumpObject;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        
        return ((HeapDumpInstanceMirror)obj).heapDumpObject.equals(heapDumpObject);
    }
    
    @Override
    public int hashCode() {
        return 11 * heapDumpObject.hashCode();
    }
    
    public IInstance getHeapDumpObject() {
        return heapDumpObject;
    }
    
    public HeapDumpClassMirror getClassMirror() {
        return (HeapDumpClassMirror)vm.makeMirror(heapDumpObject.getClazz());
    }
    
    @Override
    public int identityHashCode() {
        return vm.identityHashCode(heapDumpObject);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + heapDumpObject;
    }

    @Override
    public Object getBoxedValue(FieldMirror field) throws IllegalAccessException {
        // Need to account for field shadowing manually
        ClassMirror thisClass = getClassMirror();
        for (Field f : heapDumpObject.getFields()) {
            if (f.getName().equals(field.getName())) {
                // Move up the hierarchy until we find the next field of this name
                for (;;) {
                    try {
                        thisClass.getDeclaredField(field.getName());
                        break;
                    } catch (NoSuchFieldException e) {
                        thisClass = thisClass.getSuperClassMirror();
                    }
                }
                
                if (thisClass.equals(field.getDeclaringClass())) {
                    return f.getValue();
                } else {
                    thisClass = thisClass.getSuperClassMirror();
                }
            }
        }
        
        throw new InternalError();
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
