package edu.ubc.mirrors.raw;

import static edu.ubc.mirrors.mirages.MirageClassGenerator.makeArrayType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class ArrayClassMirror extends ClassMirror {

    // May be null
    private final ClassMirrorLoader loader;
    
    private final ClassMirror elementClassMirror;
    private final int dims;
    
    public ArrayClassMirror(ClassMirrorLoader loader, int dims, ClassMirror elementClassMirror) {
        this.loader = loader;
        this.elementClassMirror = elementClassMirror;
        this.dims = dims;
    }
    
    @Override
    public VirtualMachineMirror getVM() {
        return elementClassMirror.getVM();
    }
    
    private Type getArrayType() {
        return makeArrayType(dims, Type.getObjectType(elementClassMirror.getClassName().replace('.', '/')));
    }
    
    @Override
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        throw new NoSuchFieldException(name);
    }

    @Override
    public List<FieldMirror> getMemberFields() {
        return Collections.emptyList();
    }
    
    @Override
    public FieldMirror getStaticField(String name) throws NoSuchFieldException {
        throw new NoSuchFieldException(name);
    }
    
    @Override
    public String getClassName() {
        // Don't use Type#getClassName() - that will return strings like com.foo.Bar[]
        // rather than [Lcom.foo.Bar;
        return getArrayType().getInternalName().replace('/', '.');
    }

    @Override
    public ClassMirrorLoader getLoader() {
        return loader;
    }

    @Override
    public byte[] getBytecode() {
        return null;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public ClassMirror getComponentClassMirror() {
        if (dims == 1) {
            return elementClassMirror;
        } else { 
            return new ArrayClassMirror(loader, dims - 1, elementClassMirror);
        }
    }

    @Override
    public ClassMirror getSuperClassMirror() {
        return loadClassMirrorInternal(Object.class.getName());
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public List<ClassMirror> getInterfaceMirrors() {
        List<ClassMirror> result = new ArrayList<ClassMirror>(2);
        result.add(loadClassMirrorInternal(Cloneable.class.getName()));
        result.add(loadClassMirrorInternal(Serializable.class.getName()));
        return result;
    }
    
    @Override
    public List<String> getDeclaredFieldNames() {
        return Collections.emptyList();
    }
    
    @Override
    public List<InstanceMirror> getInstances() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        return getClass().getName() + ": " + getArrayType();
    }
}
