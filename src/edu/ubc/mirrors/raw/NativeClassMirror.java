package edu.ubc.mirrors.raw;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.raw.NativeObjectMirror.NativeFieldMirror;

public class NativeClassMirror implements ClassMirror {

    private final Class<?> klass;
    
    public NativeClassMirror(Class<?> klass) {
        this.klass = klass;
    }
    
    public String getClassName() {
        return klass.getName();
    }
    
    @Override
    public InputStream getBytecodeStream() {
        return getNativeBytecodeStream(klass.getClassLoader(), klass.getName());
    }
    
    public static InputStream getNativeBytecodeStream(ClassLoader classLoader, String name) {
     // TODO-RS: Need instrumentation for fully general solution...
        String resourceName = name.replace('.', '/') + ".class";
        if (classLoader == null) {
            return ClassLoader.getSystemResourceAsStream(resourceName);
        } else {
            return classLoader.getResourceAsStream(resourceName);
        }
    }
    
    public ClassMirror getSuperClassMirror() {
        Class<?> superclass = klass.getSuperclass();
        return superclass != null ? new NativeClassMirror(superclass) : null;
    }
    
    public boolean isArray() {
        return klass.isArray();
    }
    
    public ClassMirror getComponentClassMirror() {
        Class<?> componentType = klass.getComponentType();
        return componentType != null ? new NativeClassMirror(componentType) : null;
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
                return new NativeClassMirror(superclass).getStaticField(name);
            } catch (NoSuchFieldException e) {
                // Ignore
            }
        }
        
        for (Class<?> i : klass.getInterfaces()) {
            try {
                return new NativeClassMirror(i).getStaticField(name);
            } catch (NoSuchFieldException e) {
                // Ignore
            }
        }
        
        throw new NoSuchFieldException();
    }
    
    @Override
    public boolean isInterface() {
        return klass.isInterface();
    }
    
    @Override
    public List<ClassMirror> getInterfaceMirrors() {
        List<ClassMirror> result = new ArrayList<ClassMirror>();
        for (Class<?> i : klass.getInterfaces()) {
            result.add(new NativeClassMirror(i));
        }
        return result;
    }
}
