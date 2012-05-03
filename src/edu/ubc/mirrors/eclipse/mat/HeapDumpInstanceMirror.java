package edu.ubc.mirrors.eclipse.mat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IInstance;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IObjectArray;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;

public class HeapDumpInstanceMirror implements InstanceMirror, HeapDumpObjectMirror {

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
    
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        List<Field> fields = heapDumpObject.getFields();
        for (Field field : fields) {
            if (field.getName().equals(name)) {
                return new HeapDumpFieldMirror(vm, field);
            }
        }
        throw new NoSuchFieldException(name);
    }

    @Override
    public List<FieldMirror> getMemberFields() {
        List<Field> fields = heapDumpObject.getFields();
        List<FieldMirror> result = new ArrayList<FieldMirror>(fields.size());
        for (Field field : fields) {
            result.add(new HeapDumpFieldMirror(vm, field));
        }
        return result;
    }
    
    public HeapDumpClassMirror getClassMirror() {
        return (HeapDumpClassMirror)vm.makeMirror(heapDumpObject.getClazz());
    }

    
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + heapDumpObject;
    }
}
