package edu.ubc.mirrors.jhat;

import java.util.List;

import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IInstance;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;

public class HeapDumpClassMirror implements ClassMirror<Object> {

    private final IClass klass;
    
    public HeapDumpClassMirror(IClass klass) {
        this.klass = klass;
    }
    
    public Class<?> getMirroredClass() {
        try {
            return Class.forName(klass.getName());
        } catch (ClassNotFoundException e) {
            // TODO-RS: This is a real possibility - handle correctly!
            throw new InternalError();
        }
    }

    public FieldMirror getStaticField(String name) throws NoSuchFieldException {
        List<Field> fields = klass.getStaticFields();
        for (Field field : fields) {
            if (field.getName().equals(name)) {
                return new HeapDumpFieldMirror(field);
            }
        }
        throw new NoSuchFieldException(name);
    }

}
