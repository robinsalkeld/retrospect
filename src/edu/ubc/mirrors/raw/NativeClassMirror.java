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
    
    public boolean isArray() {
        return klass.isArray();
    }
    
    public ClassMirror<?> getComponentClassMirror() {
        Class<?> componentType = klass.getComponentType();
        return componentType != null ? new NativeClassMirror<Object>(componentType) : null;
    }
    
    public FieldMirror getStaticField(String name) throws NoSuchFieldException {
        Field field = klass.getDeclaredField(name);
        if (!Modifier.isStatic(field.getModifiers())) {
            // Crap, fall back to manual search
            field = findField(name);
        }
        return new NativeFieldMirror(field, null);
    }
    
    private Field findField(String name) throws NoSuchFieldException {
        for (Field f : klass.getDeclaredFields()) {
            if (f.getName().equals(name) && Modifier.isStatic(f.getModifiers())) {
                return f;
            }
        }
        throw new NoSuchFieldException(name);
    }
}
