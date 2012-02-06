package edu.ubc.mirrors.jhat;

import java.util.List;

import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.IArray;
import org.eclipse.mat.snapshot.model.IInstance;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IObjectArray;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;

import hat.model.JavaField;
import hat.model.JavaObject;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.mirages.MirageClassLoader;

public class HeapDumpObjectMirror implements ObjectMirror<Object> {

    private final MirageClassLoader loader;
    private final IInstance heapDumpObject;
    
    public HeapDumpObjectMirror(MirageClassLoader loader, IInstance heapDumpObject) {
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

    public ClassMirror<?> getClassMirror() {
        return new HeapDumpClassMirror(loader, heapDumpObject.getClazz());
    }

    public static ObjectMirror<?> makeMirror(MirageClassLoader loader, IObject object) {
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
