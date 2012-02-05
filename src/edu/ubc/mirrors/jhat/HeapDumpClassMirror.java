package edu.ubc.mirrors.jhat;

import java.util.List;

import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.IClass;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;

public class HeapDumpClassMirror implements ClassMirror<Object> {

    private final HeapDumpClassMirrorLoader loader;
    private final IClass klass;
    
    public HeapDumpClassMirror(HeapDumpClassMirrorLoader loader, IClass klass) {
        this.loader = loader;
        this.klass = klass;
    }
    
    public String getClassName() {
        return klass.getName();
    }

    public boolean isArray() {
        return klass.isArrayType();
    }
    
    public ClassMirror<?> getComponentClassMirror() {
        // Takes some work - not directly exposed by IClass, but
        // can be inferred by manually looking up the name.
        if (!isArray()) {
            return null;
        }
        String componentName = klass.getName().substring(1);
        try {
            return loader.loadClassMirror(componentName);
        } catch (ClassNotFoundException e) {
            // Should never happen
            throw new InternalError();
        }
    }
    
    public FieldMirror getStaticField(String name) throws NoSuchFieldException {
        List<Field> fields = klass.getStaticFields();
        for (Field field : fields) {
            if (field.getName().equals(name)) {
                return new HeapDumpFieldMirror(loader.getMirageClassLoader(), field);
            }
        }
        throw new NoSuchFieldException(name);
    }

}
