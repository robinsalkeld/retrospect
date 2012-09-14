package edu.ubc.mirrors.raw;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.fieldmap.ClassFieldMirror;

public class PrimitiveClassMirror implements ClassMirror {

    private final VirtualMachineMirror vm;
    private final String typeName;
    
    public PrimitiveClassMirror(VirtualMachineMirror vm, String typeName) {
        this.vm = vm;
        this.typeName = typeName;
    }
    
    @Override
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        return new ClassFieldMirror(this, name);
    }

    @Override
    public List<FieldMirror> getMemberFields() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassMirror getClassMirror() {
        return vm.findBootstrapClassMirror(Class.class.getName());
    }

    @Override
    public VirtualMachineMirror getVM() {
        return vm;
    }

    @Override
    public String getClassName() {
        return typeName;
    }

    @Override
    public ClassMirrorLoader getLoader() {
        return null;
    }

    @Override
    public byte[] getBytecode() {
        return null;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public ClassMirror getComponentClassMirror() {
        return null;
    }

    @Override
    public ClassMirror getSuperClassMirror() {
        return null;
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public List<ClassMirror> getInterfaceMirrors() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, ClassMirror> getDeclaredFields() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldMirror getStaticField(String name) throws NoSuchFieldException {
        throw new NoSuchFieldException(name);
    }

    @Override
    public List<InstanceMirror> getInstances() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MethodMirror getMethod(String name, ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConstructorMirror getConstructor(ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ConstructorMirror> getDeclaredConstructors(boolean publicOnly) {
        return Collections.emptyList();
    }

    @Override
    public List<MethodMirror> getDeclaredMethods(boolean publicOnly) {
        return Collections.emptyList();
    }

    @Override
    public int getModifiers() {
        return Modifier.PUBLIC;
    }

    @Override
    public InstanceMirror newRawInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArrayMirror newArray(int size) {
        // TODO-RS: Could support this...
        throw new UnsupportedOperationException();
    }

    @Override
    public ArrayMirror newArray(int... dims) {
        // TODO-RS: Could support this...
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean initialized() {
        return true;
    }

    @Override
    public byte[] getRawAnnotations() {
        return null;
    }

}
