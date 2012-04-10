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


public abstract class ClassMirror implements InstanceMirror {

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
    
    /* TODO-RS: this stuff doesn't belong here! Move to a base class or wrapper! */
    
    
    
    public MethodMirror getInstanceMethod(String name, ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException {
        return findMethod(name, paramTypes, false);
    }
    
    public MethodMirror getStaticMethod(String name, ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException {
        return findMethod(name, paramTypes, true);
    }
    
    private MethodMirror findMethod(String name, ClassMirror[] paramTypes, boolean isStatic) throws SecurityException, NoSuchMethodException {
        Class<?>[] mirageParamTypes = new Class<?>[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            mirageParamTypes[i] = MirageClassLoader.loadMirageClass(paramTypes[i]);
        }
        Class<?> mirageClass = MirageClassLoader.loadMirageClass(this);
        Method method = mirageClass.getMethod(name, mirageParamTypes);
        if (Modifier.isStatic(method.getModifiers()) != isStatic) {
            throw new UnsupportedOperationException("Darnit! Need manual search");
        }
        return new MirageMethod(method);
    }
    
    private static class MirageMethod implements MethodMirror {

        private final Method mirageClassMethod;
        
        public MirageMethod(Method method) {
            this.mirageClassMethod = method;
        }
        
        @Override
        public ObjectMirror invoke(InstanceMirror obj, ObjectMirror... args) throws IllegalAccessException, InvocationTargetException {
            Object[] mirageArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                mirageArgs[i] = MirageClassLoader.makeMirageStatic(args[i]);
            }
            Object result = mirageClassMethod.invoke(obj, mirageArgs);
            return Reflection.getMirror(result);
        }
        
    }
    
    public Class<?> getNativeStubsClass() {
        return NativeClassMirror.getNativeStubsClass(getClassName());
    }
    
    @Override
    public ClassMirror getClassMirror() {
        return loadClassMirrorInternal(Class.class.getName());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ClassMirror)) {
            return false;
        }
        
        return getClassName().equals(((ClassMirror)obj).getClassName());
    }
    
    @Override
    public int hashCode() {
        return 7 + getClassName().hashCode();
    }
    
    public boolean isAssignableFrom(ClassMirror other) {
        if (equals(other)) {
            return true;
        }
        
        if (isArray()) {
            return other.isArray() && getComponentClassMirror().isAssignableFrom(other.getComponentClassMirror());
        }
        
        if (other.isInterface() && getClassName().equals(Object.class.getName())) {
            return true; 
        }
        ClassMirror otherSuperclass = other.getSuperClassMirror();
        if (otherSuperclass != null && isAssignableFrom(otherSuperclass)) {
            return true;
        }

        for (ClassMirror interfaceNode : other.getInterfaceMirrors()) {
            if (isAssignableFrom(interfaceNode)) {
                return true;
            }
        }
        
        return false;
    }
    
    protected ClassMirror loadClassMirrorInternal(String name) {
        try {
            return ClassStubs.classMirrorForName(name, false, getLoader());
        } catch (ClassNotFoundException e) {
            NoClassDefFoundError error = new NoClassDefFoundError(e.getMessage());
            error.initCause(e);
            throw error;
        }
    }
    
}
