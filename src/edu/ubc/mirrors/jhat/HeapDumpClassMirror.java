package edu.ubc.mirrors.jhat;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.IClass;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.raw.NativeClassMirror;

public class HeapDumpClassMirror extends ClassMirror {

    private final HeapDumpClassMirrorLoader loader;
    private final IClass klass;
    
    public HeapDumpClassMirror(HeapDumpClassMirrorLoader loader, IClass klass) {
        this.loader = loader;
        this.klass = klass;
    }
    
    public String getClassName() {
        String name = klass.getName();
        if (name.endsWith("[]")) {
            name = "L" + name;
            while (name.endsWith("[]")) {
                name = "[" + name.substring(0, name.length() - 2);
            }
            name = name + ";";
        }
        return name;
    }
    
    @Override
    public byte[] getBytecode() {
        return NativeClassMirror.getNativeBytecode(loader.getClassLoader(), getClassName());
    }

    public boolean isArray() {
        return klass.isArrayType();
    }
    
    public ClassMirror getComponentClassMirror() {
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
    
    public ClassMirror getSuperClassMirror() {
        IClass superclass = klass.getSuperClass();
        return superclass != null ? new HeapDumpClassMirror(loader, superclass) : null;
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
    
    private Class<?> loadClass() {
        try {
            return loader.getClassLoader().loadClass(klass.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public boolean isInterface() {
        return loadClass().isInterface();
    }
    
    @Override
    public List<ClassMirror> getInterfaceMirrors() {
        List<ClassMirror> result = new ArrayList<ClassMirror>();
        for (Class<?> i : loadClass().getInterfaces()) {
            result.add(new NativeClassMirror(i));
        }
        return result;
    }

    @Override
    public ClassMirrorLoader getLoader() {
        return loader;
    }

}
