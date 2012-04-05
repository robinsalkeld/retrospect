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

public class ArrayClassMirror extends ClassMirror {

    private final ClassMirrorLoader loader;
    private final Type elementType;
    private final int dims;
    
    public ArrayClassMirror(ClassMirrorLoader loader, Type type) {
        this.loader = loader;
        this.elementType = type.getElementType();
        this.dims = type.getDimensions();
    }
    
    @Override
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<FieldMirror> getMemberFields() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public FieldMirror getStaticField(String name) throws NoSuchFieldException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String getClassName() {
        // Don't use getClassName() - that will return strings like com.foo.Bar[]
        // rather than [Lcom.foo.Bar;
        return makeArrayType(dims, elementType).getInternalName().replace('/', '.');
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
            if (elementType.equals(Type.BOOLEAN_TYPE)) {
                return new NativeClassMirror(Boolean.TYPE);
            } else if (elementType.equals(Type.BYTE_TYPE)) {
                return new NativeClassMirror(Byte.TYPE);
            } else if (elementType.equals(Type.CHAR_TYPE)) {
                return new NativeClassMirror(Character.TYPE);
            } else if (elementType.equals(Type.SHORT_TYPE)) {
                return new NativeClassMirror(Short.TYPE);
            } else if (elementType.equals(Type.INT_TYPE)) {
                return new NativeClassMirror(Integer.TYPE);
            } else if (elementType.equals(Type.LONG_TYPE)) {
                return new NativeClassMirror(Long.TYPE);
            } else if (elementType.equals(Type.FLOAT_TYPE)) {
                return new NativeClassMirror(Float.TYPE);
            } else if (elementType.equals(Type.DOUBLE_TYPE)) {
                return new NativeClassMirror(Double.TYPE);
            } else {
                return loadClassMirrorInternal(elementType.getClassName());
            }
        } else { 
            return loadClassMirrorInternal(makeArrayType(dims - 1, elementType).getInternalName().replace('/', '.'));
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
    public String toString() {
        return getClass().getName() + ": " + makeArrayType(dims, elementType);
    }
}
