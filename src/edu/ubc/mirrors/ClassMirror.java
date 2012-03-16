package edu.ubc.mirrors;

import java.util.List;


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
    
    public abstract FieldMirror getStaticField(String name) throws NoSuchFieldException;
    
    public Class<?> getNativeStubsClass() {
        return null;
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
            return getLoader().loadClassMirror(name);
        } catch (ClassNotFoundException e) {
            NoClassDefFoundError error = new NoClassDefFoundError(e.getMessage());
            error.initCause(e);
            throw error;
        }
    }
    
}
