package edu.ubc.mirrors;

import java.util.List;
import java.util.Map;


public interface ClassMirror extends InstanceMirror {

    public abstract VirtualMachineMirror getVM();
    
    public abstract String getClassName();
    
    public abstract ClassMirrorLoader getLoader();
    
    public abstract byte[] getBytecode();
    
    public abstract boolean isPrimitive();
    
    public abstract boolean isArray();
    
    public abstract ClassMirror getComponentClassMirror();
    
    public abstract ClassMirror getSuperClassMirror();
    
    public abstract boolean isInterface();
    
    public abstract List<ClassMirror> getInterfaceMirrors();
    
    public abstract Map<String, ClassMirror> getDeclaredFields();
    
    public abstract FieldMirror getStaticField(String name) throws NoSuchFieldException;
    
    public abstract List<InstanceMirror> getInstances();

    public abstract MethodMirror getMethod(String name, ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException;

    public abstract ConstructorMirror getConstructor(ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException;
    
    public abstract List<ConstructorMirror> getDeclaredConstructors(boolean publicOnly);
    
    public abstract int getModifiers();
    
    public abstract InstanceMirror newRawInstance();
    
    public abstract ArrayMirror newArray(int size);
    
    public abstract ArrayMirror newArray(int... dims);
    
    public abstract boolean initialized();
}
