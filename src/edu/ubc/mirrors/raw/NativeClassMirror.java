package edu.ubc.mirrors.raw;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.raw.NativeObjectMirror.NativeFieldMirror;
import edu.ubc.mirrors.raw.nativestubs.java.lang.ClassStubs;
import edu.ubc.mirrors.raw.nativestubs.java.lang.SystemStubs;
import edu.ubc.mirrors.raw.nativestubs.java.lang.ThreadStubs;

public class NativeClassMirror extends ClassMirror {

    private final Class<?> klass;
    
    public NativeClassMirror(Class<?> klass) {
        this.klass = klass;
    }
    
    public NativeClassMirror(ClassLoader loader, String className) {
        try {
            this.klass = Class.forName(className, false, loader);
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(className);
        }
    }
    
    public Class<?> getKlass() {
        return klass;
    }
    
    public String getClassName() {
        return klass.getName();
    }
    
    protected ClassMirror findClassMirror(Class<?> forClass) {
        return new NativeClassMirror(forClass);
    }
    
    @Override
    public byte[] getBytecode() {
        return getNativeBytecode(klass.getClassLoader(), klass.getName());
    }
    
    public static byte[] getNativeBytecode(ClassLoader classLoader, String name) {
     // TODO-RS: Need instrumentation for fully general solution...
        String resourceName = name.replace('.', '/') + ".class";
        InputStream bytesIn;
        if (classLoader == null) {
            bytesIn = ClassLoader.getSystemResourceAsStream(resourceName);
        } else {
            bytesIn = classLoader.getResourceAsStream(resourceName);
        }
        
        return bytesIn == null ? null : readFully(bytesIn);
    }
    
    public static byte[] readFully(InputStream bytesIn) {
        int read;
        byte[] buffer = new byte[16384];
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        try {
            while ((read = bytesIn.read(buffer)) != -1) {
                bytesOut.write(buffer, 0, read);
            }
            bytesIn.close();
        } catch (IOException e) {
            throw new InternalError();
        }
        return bytesOut.toByteArray();
    }
    
    @Override
    public boolean isPrimitive() {
        return klass.isPrimitive();
    }
    
    public ClassMirror getSuperClassMirror() {
        Class<?> superclass = klass.getSuperclass();
        return superclass != null ? findClassMirror(superclass) : null;
    }
    
    public boolean isArray() {
        return klass.isArray();
    }
    
    public ClassMirror getComponentClassMirror() {
        Class<?> componentType = klass.getComponentType();
        return componentType != null ? findClassMirror(componentType) : null;
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
            result.add(findClassMirror(i));
        }
        return result;
    }

    @Override
    public ClassMirrorLoader getLoader() {
        return klass.getClassLoader() == null ? null : new NativeClassMirrorLoader(klass.getClassLoader());
    }

    @Override
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        return NativeObjectMirror.getField(klass, name, false);
    }
    
    @Override
    public List<FieldMirror> getMemberFields() {
        return NativeObjectMirror.getMemberFields(klass);
    }

    public static Class<?> getNativeStubsClass(String name) {
        String nativeStubsName = "edu.ubc.mirrors.raw.nativestubs." + name + "Stubs";
        try {
            return Class.forName(nativeStubsName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    
    @Override
    public List<String> getDeclaredFieldNames() {
        List<String> names = new ArrayList<String>();
        for (Field f : klass.getDeclaredFields()) {
            names.add(f.getName());
        }
        return names;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + klass;
    }
}
