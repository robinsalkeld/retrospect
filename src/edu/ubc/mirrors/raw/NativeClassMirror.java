package edu.ubc.mirrors.raw;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.NativeInstanceMirror.NativeFieldMirror;
import edu.ubc.mirrors.raw.nativestubs.java.lang.ClassStubs;
import edu.ubc.mirrors.raw.nativestubs.java.lang.SystemStubs;
import edu.ubc.mirrors.raw.nativestubs.java.lang.ThreadStubs;

public class NativeClassMirror extends NativeInstanceMirror implements ClassMirror {

    private final Class<?> klass;
    
    private ClassMirror superclassMirror;
    private List<ClassMirror> interfaceMirrors;
    
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
        if (superclassMirror == null && superclass != null) {
            superclassMirror = findClassMirror(superclass);
        }
        return superclassMirror;
    }
    
    public boolean isArray() {
        return klass.isArray();
    }
    
    public ClassMirror getComponentClassMirror() {
        Class<?> componentType = klass.getComponentType();
        return componentType != null ? findClassMirror(componentType) : null;
    }
    
    public FieldMirror getStaticField(String name) throws NoSuchFieldException {
        if (klass.equals(System.class) && name.equals("security")) {
            return NativeSystemSecurityField.INSTANCE;
        }
        
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
        
        throw new NoSuchFieldException();
    }
    
    @Override
    public boolean isInterface() {
        return klass.isInterface();
    }
    
    @Override
    public List<ClassMirror> getInterfaceMirrors() {
        if (interfaceMirrors == null) {
            interfaceMirrors = new ArrayList<ClassMirror>();
            for (Class<?> i : klass.getInterfaces()) {
                interfaceMirrors.add(findClassMirror(i));
            }
        }
        return Collections.unmodifiableList(interfaceMirrors);
    }

    @Override
    public ClassMirrorLoader getLoader() {
        return klass.getClassLoader() == null ? null : new NativeClassMirrorLoader(klass.getClassLoader());
    }

    @Override
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        return NativeInstanceMirror.getField(klass, name, false);
    }
    
    @Override
    public List<FieldMirror> getMemberFields() {
        return NativeInstanceMirror.getMemberFields(klass);
    }

    @Override
    public Map<String, ClassMirror> getDeclaredFields() {
        Map<String, ClassMirror> fields = new HashMap<String, ClassMirror>();
        for (Field f : klass.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers())) {
                fields.put(f.getName(), (ClassMirror)NativeInstanceMirror.makeMirror(f.getType()));
            }
        }
        return fields;
    }
        
    @Override
    public MethodMirror getMethod(String name, ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException {
        Class<?>[] nativeParamTypes = new Class<?>[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            nativeParamTypes[i] = getNativeClass(paramTypes[i]);
        }
        Method nativeMethod = klass.getDeclaredMethod(name, nativeParamTypes);
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
    
    @Override
    public List<ConstructorMirror> getDeclaredConstructors(boolean publicOnly) {
        Constructor<?>[] nativeConstructors = publicOnly ? klass.getConstructors() : klass.getDeclaredConstructors();
        List<ConstructorMirror> result = new ArrayList<ConstructorMirror>();
        for (Constructor<?> nativeConstructor : nativeConstructors) {
            result.add(new NativeConstructorMirror(nativeConstructor));
        }
        return result;
    }
    
    @Override
    public int getModifiers() {
        return klass.getModifiers();
    }
    
    private Class<?> getNativeClass(ClassMirror mirror) {
        if (mirror instanceof NativeClassMirror) {
            return ((NativeClassMirror)mirror).getKlass();
        } else if (mirror instanceof ArrayClassMirror) {
            ArrayClassMirror classMirror = (ArrayClassMirror)mirror;
            Class<?> elementClass = getNativeClass(classMirror.getElementClassMirror());
            try {
                Type type = Reflection.typeForClassMirror(classMirror);
                return Class.forName(type.getDescriptor(), false, elementClass.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new InternalError();
            }
        } else {
            throw new IllegalArgumentException("Native class mirror expected: " + mirror);
        }
    }
    @Override
    public List<InstanceMirror> getInstances() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public InstanceMirror newRawInstance() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ArrayMirror newArray(int size) {
        return (ArrayMirror)NativeInstanceMirror.makeMirror(Array.newInstance(klass, size));
    }
    
    @Override
    public ArrayMirror newArray(int... dims) {
        return (ArrayMirror)NativeInstanceMirror.makeMirror(Array.newInstance(klass, dims));
    }
    
    @Override
    public boolean initialized() {
        return true;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + klass;
    }
}
