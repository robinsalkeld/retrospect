package edu.ubc.mirrors.eclipse.mat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IInstance;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IObjectArray;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;

public class HeapDumpInstanceMirror implements InstanceMirror, HeapDumpObjectMirror {

    private final HeapDumpClassMirrorLoader loader;
    private final IInstance heapDumpObject;
    
    public HeapDumpInstanceMirror(HeapDumpClassMirrorLoader loader, IInstance heapDumpObject) {
        this.loader = loader;
        this.heapDumpObject = heapDumpObject;
    }
    
    public IInstance getHeapDumpObject() {
        return heapDumpObject;
    }
    
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        List<Field> fields = heapDumpObject.getFields();
        for (Field field : fields) {
            if (field.getName().equals(name)) {
                return new HeapDumpFieldMirror(loader, this, field);
            }
        }
        throw new NoSuchFieldException(name);
    }

    public HeapDumpClassMirror getClassMirror() {
        return new HeapDumpClassMirror(loader, heapDumpObject.getClazz());
    }

    private static final Map<IObject, ObjectMirror> mirrors = new HashMap<IObject, ObjectMirror>();
    
    public static ObjectMirror makeMirror(HeapDumpClassMirrorLoader loader, IObject object) {
        if (object == null) {
            return null;
        }
        ObjectMirror mirror = mirrors.get(object);
        if (mirror != null) {
            return mirror;
        }
        
        if (object instanceof IClass) {
            mirror = new HeapDumpClassMirror(loader, (IClass)object);
        } else if (object instanceof IInstance) {
            mirror = new HeapDumpInstanceMirror(loader, (IInstance)object);
        } else if (object instanceof IPrimitiveArray) {
            mirror = new HeapDumpPrimitiveArrayMirror(loader, (IPrimitiveArray)object);
        } else if (object instanceof IObjectArray) {
            mirror = new HeapDumpObjectArrayMirror(loader, (IObjectArray)object);
        } else {
            throw new IllegalArgumentException("Unsupported subclass: " + object.getClass());
        }
        
        mirrors.put(object, mirror);
        return mirror;
    }
}
