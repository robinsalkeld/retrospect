package edu.ubc.mirrors.raw;

import static edu.ubc.mirrors.mirages.MirageClassGenerator.makeArrayType;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ArrayMirror;
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
    private final Type arrayType;
    
    public ArrayClassMirror(int dims, ClassMirror elementClassMirror) {
        this.elementClassMirror = elementClassMirror;
        this.dims = dims;
        this.arrayType = makeArrayType(dims, Reflection.typeForClassMirror(elementClassMirror));
    }
    
    @Override
    public VirtualMachineMirror getVM() {
        return elementClassMirror.getVM();
    }
    
    public ClassMirror getElementClassMirror() {
        return elementClassMirror;
    }
    
    public Type getArrayType() {
        return arrayType;
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
        return elementClassMirror.getVM().findBootstrapClassMirror(Object.class.getName());
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    public static List<ClassMirror> getInterfaceMirrorsForArrays(VirtualMachineMirror vm) {
        List<ClassMirror> result = new ArrayList<ClassMirror>(2);
        result.add(vm.findBootstrapClassMirror(Cloneable.class.getName()));
        result.add(vm.findBootstrapClassMirror(Serializable.class.getName()));
        return result;
    }
    
    @Override
    public List<ClassMirror> getInterfaceMirrors() {
        return getInterfaceMirrorsForArrays(elementClassMirror.getVM());
    }
    
    @Override
    public Map<String, ClassMirror> getDeclaredFields() {
        return Collections.emptyMap();
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
    public List<ConstructorMirror> getDeclaredConstructors(boolean publicOnly) {
        return Collections.emptyList();
    }
    
    @Override
    public int getModifiers() {
        return Modifier.FINAL | elementClassMirror.getModifiers();
    }
    
    @Override
    public ClassMirror getClassMirror() {
        return getVM().findBootstrapClassMirror(Class.class.getName());
    }
    
    @Override
    public InstanceMirror newRawInstance() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ArrayMirror newArray(int size) {
        // TODO-RS: Semantics are defined perfectly well by java.lang.reflect.Array#newInstance,
        // but the holograms architecture won't call this.
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ArrayMirror newArray(int... dims) {
     // TODO-RS: Semantics are defined perfectly well by java.lang.reflect.Array#newInstance,
        // but the holograms architecture won't call this.
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean initialized() {
        return true;
    }

    public int getDimensions() {
        return dims;
    }
    
    @Override
    public byte[] getRawAnnotations() {
        // TODO-RS: Hope this is right...
        return new byte[0];
    }
}
