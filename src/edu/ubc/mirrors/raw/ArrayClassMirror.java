package edu.ubc.mirrors.raw;

import static edu.ubc.mirrors.mirages.MirageClassGenerator.makeArrayType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.mirages.Reflection;

public class ArrayClassMirror implements ClassMirror {

    private final ClassMirror elementClassMirror;
    private final int dims;
    
    public ArrayClassMirror(int dims, ClassMirror elementClassMirror) {
        this.elementClassMirror = elementClassMirror;
        this.dims = dims;
    }
    
    @Override
    public VirtualMachineMirror getVM() {
        return elementClassMirror.getVM();
    }
    
    private Type getArrayType() {
        return makeArrayType(dims, getType(elementClassMirror));
    }
    
    private Type getType(ClassMirror classMirror) {
        String name = classMirror.getClassName();
        if (classMirror.isPrimitive()) {
            if (name.equals("int")) {
                return Type.INT_TYPE;
            } else if (name.equals("void")) {
                return Type.VOID_TYPE;
            } else if (name.equals("boolean")) {
                return Type.BOOLEAN_TYPE;
            } else if (name.equals("byte")) {
                return Type.BYTE_TYPE;
            } else if (name.equals("char")) {
                return Type.CHAR_TYPE;
            } else if (name.equals("short")) {
                return Type.SHORT_TYPE;
            } else if (name.equals("double")) {
                return Type.DOUBLE_TYPE;
            } else if (name.equals("float")) {
                return Type.FLOAT_TYPE;
            } else /* if (name.equals("long")) */{
                return Type.LONG_TYPE;
            }
        } else {
            return Type.getObjectType(name.replace('.', '/'));
        }
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
        return elementClassMirror.getLoader();
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
            return new ArrayClassMirror(dims - 1, elementClassMirror);
        }
    }

    @Override
    public ClassMirror getSuperClassMirror() {
        return Reflection.loadClassMirrorInternal(this, Object.class.getName());
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public List<ClassMirror> getInterfaceMirrors() {
        List<ClassMirror> result = new ArrayList<ClassMirror>(2);
        result.add(Reflection.loadClassMirrorInternal(this, Cloneable.class.getName()));
        result.add(Reflection.loadClassMirrorInternal(this, Serializable.class.getName()));
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

    @Override
    public MethodMirror getMethod(String name, ClassMirror... paramTypes)
            throws SecurityException, NoSuchMethodException {
        
        throw new NoSuchMethodException(name);
    }

    @Override
    public ConstructorMirror getConstructor(ClassMirror... paramTypes)
            throws SecurityException, NoSuchMethodException {
        
        throw new NoSuchMethodException();
    }
    
    @Override
    public ClassMirror getClassMirror() {
        return getVM().findBootstrapClassMirror(Class.class.getName());
    }
}
