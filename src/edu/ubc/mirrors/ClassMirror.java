package edu.ubc.mirrors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.raw.nativestubs.java.lang.ClassStubs;


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
    
    public abstract List<String> getDeclaredFieldNames();
    
    public abstract FieldMirror getStaticField(String name) throws NoSuchFieldException;
    
    public abstract List<InstanceMirror> getInstances();

    public abstract MethodMirror getMethod(String name, ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException;

    public abstract ConstructorMirror getConstructor(ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException;
}
