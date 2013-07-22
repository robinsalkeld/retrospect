package edu.ubc.mirrors;

import java.util.List;


public interface ClassMirror extends InstanceMirror {

    public abstract VirtualMachineMirror getVM();
    
    public abstract String getClassName();
    
    public abstract ClassMirrorLoader getLoader();
    
    public abstract byte[] getBytecode();
    
    public abstract boolean isPrimitive();
    
    public boolean isArray();
    
    public ClassMirror getComponentClassMirror();
    
    public ClassMirror getSuperClassMirror();
    
    public boolean isInterface();
    
    public List<ClassMirror> getInterfaceMirrors();
    
    public FieldMirror getDeclaredField(String name);
    
    public List<FieldMirror> getDeclaredFields();
    
    public List<ObjectMirror> getInstances();

    public MethodMirror getDeclaredMethod(String name, ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException;
    public MethodMirror getMethod(String name, ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException;

    public ConstructorMirror getConstructor(ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException;
    
    public List<ConstructorMirror> getDeclaredConstructors(boolean publicOnly);
    
    public List<MethodMirror> getDeclaredMethods(boolean publicOnly); 
 
    public int getModifiers();
    
    public InstanceMirror newRawInstance();
    
    public ArrayMirror newArray(int size);
    
    public ArrayMirror newArray(int... dims);
    
    public boolean initialized();
    
    // It would feel more natural to return the parsed annotations (i.e. InstanceMirrors)
    // but this works better since the annotations are parsed and instantiated in normal Java code
    // rather than in the VM itself.
    public byte[] getRawAnnotations();
    
    // This fake "object" must return the vm's Object type from getClassMirror()
    public InstanceMirror getStaticFieldValues();
}
