package edu.ubc.mirrors.jhat;

import java.util.List;

import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.IInstance;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IObjectArray;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;

public class HeapDumpObjectMirror implements InstanceMirror {

    private final HeapDumpClassMirrorLoader loader;
    private final IInstance heapDumpObject;
    
    public HeapDumpObjectMirror(HeapDumpClassMirrorLoader loader, IInstance heapDumpObject) {
        this.loader = loader;
        this.heapDumpObject = heapDumpObject;
    }
    
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        List<Field> fields = heapDumpObject.getFields();
        for (Field field : fields) {
            if (field.getName().equals(name)) {
                return new HeapDumpFieldMirror(loader, field);
            }
        }
        throw new NoSuchFieldException(name);
    }

    public ClassMirror getClassMirror() {
        return new HeapDumpClassMirror(loader, heapDumpObject.getClazz());
    }

    public static ObjectMirror makeMirror(HeapDumpClassMirrorLoader loader, IObject object) {
        if (object == null) {
            return null;
        }
        if (object instanceof IInstance) {
            return new HeapDumpObjectMirror(loader, (IInstance)object);
        } else if (object instanceof IPrimitiveArray) {
            return new HeapDumpPrimitiveArrayMirror(loader, (IPrimitiveArray)object);
        } else if (object instanceof IObjectArray) {
            return new HeapDumpObjectArrayMirror(loader, (IObjectArray)object);
        } else {
            throw new IllegalArgumentException("Unsupported subclass: " + object.getClass());
        }
    }
}
