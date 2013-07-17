package edu.ubc.mirrors.raw;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.StaticFieldValuesMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class NativeClassMirror extends NativeInstanceMirror implements ClassMirror {

    private final class NativeStaticFieldValuesMirror extends NativeInstanceMirror implements StaticFieldValuesMirror {
        private NativeStaticFieldValuesMirror() {
            super(null);
        }

        public ClassMirror getClassMirror() {
            return vm.findBootstrapClassMirror(Object.class.getName());
        }

        @Override
        public ClassMirror forClassMirror() {
            return NativeClassMirror.this;
        }
        
        public ObjectMirror get(FieldMirror field) throws IllegalAccessException {
            // Special case for java.lang.System.security, which is hidden from the reflective API.
            if (getClassName().equals(System.class.getName()) && field.getName().equals("security")) {
                return NativeInstanceMirror.makeMirror(System.getSecurityManager());
            } else {
                return super.get(field);
            }
        }

        public void set(FieldMirror field, ObjectMirror o) throws IllegalAccessException {
            // Special case for java.lang.System.security, which is hidden from the reflective API.
            if (getClassName().equals(System.class.getName()) && field.getName().equals("security")) {
                Object nativeValue = ((NativeInstanceMirror)o).getNativeObject();
                System.setSecurityManager((SecurityManager)nativeValue);
            } else {
                super.set(field, o);
            }
        }
    }

    private final Class<?> klass;
    
    private ClassMirror superclassMirror;
    private List<ClassMirror> interfaceMirrors;
    
    private final VirtualMachineMirror vm;
    
    public NativeClassMirror(Class<?> klass) {
        super(klass);
        this.klass = klass;
        this.vm = NativeVirtualMachineMirror.INSTANCE;
    }
    
    public NativeClassMirror(Class<?> klass, VirtualMachineMirror vm) {
        super(klass);
        this.klass = klass;
        if (!klass.isPrimitive()) {
            throw new IllegalArgumentException("This constructor is only for primitive classes");
        }
        this.vm = vm;
    }
    
    @Override
    public VirtualMachineMirror getVM() {
        return vm;
    }
    
    public Class<?> getKlass() {
        return klass;
    }
    
    @Override
    public ClassMirror getClassMirror() {
        return vm.findBootstrapClassMirror(Class.class.getName());
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
        Field field = klass.getDeclaredField(name);
        if (Modifier.isStatic(field.getModifiers())) {
            return new NativeFieldMirror(field);
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
    public FieldMirror getDeclaredField(String name) {
        try {
            return new NativeFieldMirror(klass.getDeclaredField(name));
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
    
    @Override
    public List<FieldMirror> getDeclaredFields() {
        List<FieldMirror> fields = new ArrayList<FieldMirror>();
        for (Field f : klass.getDeclaredFields()) {
            fields.add(new NativeFieldMirror(f));
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
    public List<MethodMirror> getDeclaredMethods(boolean publicOnly) {
        Method[] nativeMethods = publicOnly ? klass.getMethods() : klass.getDeclaredMethods();
        List<MethodMirror> result = new ArrayList<MethodMirror>();
        for (Method nativeMethod : nativeMethods) {
            result.add(new NativeMethodMirror(nativeMethod));
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
    public List<ObjectMirror> getInstances() {
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
    public byte[] getRawAnnotations() {
        // TODO-RS: For now...
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + klass;
    }

    private final InstanceMirror staticFieldValues = new NativeStaticFieldValuesMirror();
    
    @Override
    public InstanceMirror getStaticFieldValues() {
        return staticFieldValues;
    }
}
