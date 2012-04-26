package edu.ubc.mirrors.raw;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.raw.NativeObjectMirror.NativeFieldMirror;
import edu.ubc.mirrors.raw.nativestubs.java.lang.ClassStubs;
import edu.ubc.mirrors.raw.nativestubs.java.lang.SystemStubs;
import edu.ubc.mirrors.raw.nativestubs.java.lang.ThreadStubs;

public class NativeClassMirror extends NativeObjectMirror implements ClassMirror {

    private final Class<?> klass;
    
    public NativeClassMirror(Class<?> klass) {
        super(klass);
        this.klass = klass;
    }
    
    @Override
    public VirtualMachineMirror getVM() {
        return NativeVirtualMachineMirror.INSTANCE;
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
    public MethodMirror getMethod(String name, ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException {
        Class<?>[] nativeParamTypes = new Class<?>[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            nativeParamTypes[i] = getNativeClass(paramTypes[i]);
        }
        Method nativeMethod = klass.getMethod(name, nativeParamTypes);
        return new NativeMethodMirror(nativeMethod);
    }

    @Override
    public ConstructorMirror getConstructor(ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException {
        Class<?>[] nativeParamTypes = new Class<?>[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            nativeParamTypes[i] = getNativeClass(paramTypes[i]);
        }
        Constructor<?> nativeConstructor = klass.getConstructor(nativeParamTypes);
        return new NativeConstructorMirror(nativeConstructor);
    }
    
    private Class<?> getNativeClass(ClassMirror mirror) {
        if (mirror instanceof NativeClassMirror) {
            return ((NativeClassMirror)mirror).getKlass();
        } else {
            throw new IllegalArgumentException("Native class mirror expected: " + mirror);
        }
    }
    @Override
    public List<InstanceMirror> getInstances() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + klass;
    }
}
