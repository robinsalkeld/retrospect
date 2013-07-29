package edu.ubc.mirrors.raw;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.BlankClassMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class PrimitiveClassMirror extends BlankClassMirror implements ClassMirror {

    private final VirtualMachineMirror vm;
    private final String typeName;
    private final String signature;
    
    public PrimitiveClassMirror(VirtualMachineMirror vm, String typeName, String signature) {
        this.vm = vm;
        this.typeName = typeName;
        this.signature = signature;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PrimitiveClassMirror)) {
            return false;
        }
        
        PrimitiveClassMirror other = (PrimitiveClassMirror)obj;
        return vm.equals(other.vm)
            && typeName.equals(other.typeName);
    }
    
    @Override
    public int hashCode() {
        return typeName.hashCode() * 32 + typeName.hashCode();
    }
    
    @Override
    public FieldMirror getDeclaredField(String name) {
        return null;
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
    public String getSignature() {
        return signature;
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
    public List<FieldMirror> getDeclaredFields() {
        return Collections.emptyList();
    }

    @Override
    public List<ObjectMirror> getInstances() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MethodMirror getDeclaredMethod(String name, ClassMirror... paramTypes)
            throws SecurityException, NoSuchMethodException {
        
        throw new NoSuchMethodException(name);
    }

    @Override
    public MethodMirror getMethod(String name, ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException {
        throw new NoSuchMethodException(name);
    }

    @Override
    public ConstructorMirror getConstructor(ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException {
        throw new NoSuchMethodException();
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
