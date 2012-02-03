package edu.ubc.mirrors.jhat;

import java.util.List;

import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.IClass;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.MirageClassLoader;

public class HeapDumpClassMirror implements ClassMirror<Object> {

    private final MirageClassLoader loader;
    private final IClass klass;
    
    public HeapDumpClassMirror(MirageClassLoader loader, IClass klass) {
        this.loader = loader;
        this.klass = klass;
    }
    
    public String getClassName() {
        return klass.getName();
    }

    public FieldMirror getStaticField(String name) throws NoSuchFieldException {
        List<Field> fields = klass.getStaticFields();
        for (Field field : fields) {
            if (field.getName().equals(name)) {
                return new HeapDumpFieldMirror(loader, field);
            }
        }
        throw new NoSuchFieldException(name);
    }

}
