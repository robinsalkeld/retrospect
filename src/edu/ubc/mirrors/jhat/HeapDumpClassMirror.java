package edu.ubc.mirrors.jhat;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.IClass;
import org.objectweb.asm.Type;

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
    
    public static String arrayElementDescriptor(String name) {
        if (name.equals("boolean")) {
            return "Z";
        } else if (name.equals("byte")) {
            return "B";
        } else if (name.equals("char")) {
            return "C";
        } else if (name.equals("short")) {
            return "S";
        } else if (name.equals("int")) {
            return "I";
        } else if (name.equals("long")) {
            return "J";
        } else if (name.equals("float")) {
            return "F";
        } else if (name.equals("double")) {
            return "D";
        } else {
            return "L" + name + ";";
        }
    }
    
    public String getClassName() {
        String name = klass.getName();
        if (name.endsWith("[]")) {
            String elementName = name;
            String dimsString = "";
            while (elementName.endsWith("[]")) {
                elementName = elementName.substring(0, elementName.length() - 2);
                dimsString += "[";
            }
            name = dimsString + arrayElementDescriptor(elementName);
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
