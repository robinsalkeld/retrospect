package edu.ubc.mirrors.raw;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.raw.NativeObjectMirror.NativeFieldMirror;

public class NativeClassMirror<T> implements ClassMirror<T> {

    private final Class<? extends T> klass;
    
    public NativeClassMirror(Class<? extends T> klass) {
        this.klass = klass;
    }
    
    public String getClassName() {
        return klass.getName();
    }
    
    public ClassMirror<?> getSuperClassMirror() {
        Class<?> superclass = klass.getSuperclass();
        return superclass != null ? new NativeClassMirror<Object>(superclass) : null;
    }
    
    public boolean isArray() {
        return klass.isArray();
    }
    
    public ClassMirror<?> getComponentClassMirror() {
        Class<?> componentType = klass.getComponentType();
        return componentType != null ? new NativeClassMirror<Object>(componentType) : null;
    }
    
    public FieldMirror getStaticField(String name) throws NoSuchFieldException {
        try {
            Field field = klass.getDeclaredField(name);
            if (Modifier.isStatic(field.getModifiers())) {
                return new NativeFieldMirror(field, null);
            }
        } catch (NoSuchFieldException e) {
            // ignore
        }
        
        // Crap, fall back to manual search
        for (Field f : klass.getDeclaredFields()) {
            if (f.getName().equals(name) && Modifier.isStatic(f.getModifiers())) {
                return new NativeFieldMirror(f, null);
            }
        }
        
        // Damn! Search the superclass and interfaces
        // TODO-RS: Check the spec on the ordering here
        Class<?> superclass = klass.getSuperclass();
        if (superclass != null) {
            try {
                return new NativeClassMirror<Object>(superclass).getStaticField(name);
            } catch (NoSuchFieldException e) {
                // Ignore
            }
        }
        
        for (Class<?> i : klass.getInterfaces()) {
            try {
                return new NativeClassMirror<Object>(i).getStaticField(name);
            } catch (NoSuchFieldException e) {
                // Ignore
            }
        }
        
        throw new NoSuchFieldException();
    }
    
    private Field findField(String name) throws NoSuchFieldException {
        
        throw new NoSuchFieldException(name);
    }
}
