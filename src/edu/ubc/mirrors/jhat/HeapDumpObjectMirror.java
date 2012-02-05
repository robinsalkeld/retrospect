package edu.ubc.mirrors.jhat;

import java.util.List;

import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.IArray;
import org.eclipse.mat.snapshot.model.IInstance;
import org.eclipse.mat.snapshot.model.IObject;

import hat.model.JavaField;
import hat.model.JavaObject;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.mirages.MirageClassLoader;

public class HeapDumpObjectMirror implements ObjectMirror<Object> {

    private final MirageClassLoader loader;
    private final IObject heapDumpObject;
    
    public HeapDumpObjectMirror(MirageClassLoader loader, IObject heapDumpObject) {
        this.loader = loader;
        this.heapDumpObject = heapDumpObject;
    }
    
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        List<Field> fields = ((IInstance)heapDumpObject).getFields();
        for (Field field : fields) {
            if (field.getName().equals(name)) {
                return new HeapDumpFieldMirror(loader, field);
            }
        }
        throw new NoSuchFieldException(name);
    }

    public int getArrayLength() {
        return ((IArray)heapDumpObject).getLength();
    }
    
    public FieldMirror getArrayElement(int index) throws ArrayIndexOutOfBoundsException {
        return new HeapDumpArrayElementMirror(loader, (IArray)heapDumpObject, index);
    }

    public ClassMirror<?> getClassMirror() {
        // TODO Auto-generated method stub
        return null;
    }

}
