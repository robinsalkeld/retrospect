package edu.ubc.mirrors.mirages;

import static edu.ubc.mirrors.mirages.MirageClassGenerator.getMirageBinaryClassName;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.raw.ArrayClassMirror;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.raw.NativeObjectMirror;
import edu.ubc.mirrors.test.ChainedClassLoader;

public class Reflection {

    public static ObjectMirror getMirror(Object o) {
        if (o instanceof Mirage) {
            return ((Mirage)o).getMirror();
        } else {
            return NativeObjectMirror.makeMirror(o);
        }
    }
    
    public static Object invoke(Object object, String name, Object... args) {
        Method match = null;
        for (Method m : object.getClass().getMethods()) {
            // TODO: Also check param types
            if (!m.isSynthetic() && m.getName().equals(name) && m.getParameterTypes().length == args.length) {
                if (match != null) {
                    throw new IllegalArgumentException("Ambiguous method name: " + object.getClass().getName() + "#" + name);
                }
                match = m;
            }
        }
        if (match == null) {
            throw new IllegalArgumentException("Method not found: " + object.getClass().getName() + "#" + name);
        }
        
        try {
            return match.invoke(object, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        
    }
    
    public static Object invoke(Class<?> klass, String name, Object... args) {
        Method match = null;
        for (Method m : klass.getMethods()) {
            // TODO: Also check param types
            if (m.getName().equals(name) && m.getParameterTypes().length == args.length) {
                if (match != null) {
                    throw new IllegalArgumentException("Ambiguous method name: " + klass.getName() + "#" + name);
                }
                match = m;
            }
        }
        if (match == null) {
            throw new IllegalArgumentException("Method not found: " + klass.getName() + "#" + name);
        }
        
        try {
            return match.invoke(null, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static ObjectMirror mirrorInvoke(ObjectMirror object, String name, ObjectMirror... args) {
        ClassMirror[] paramTypes = new ClassMirror[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i].getClassMirror();
        }
        
        MethodMirror match;
        try {
            match = object.getClassMirror().getInstanceMethod(name, paramTypes);
        } catch (NoSuchMethodException e1) {
            throw new IllegalArgumentException("Method not found: " + object.getClassMirror().getClassName() + "#" + name);
        }
        
        try {
            return match.invoke((InstanceMirror)object, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Object mirrorInvoke(ClassMirror klass, String name, ObjectMirror... args) {
        ClassMirror[] paramTypes = new ClassMirror[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i].getClassMirror();
        }
        
        MethodMirror match;
        try {
            match = klass.getStaticMethod(name, paramTypes);
        } catch (NoSuchMethodException e1) {
            throw new IllegalArgumentException("Method not found: " + klass.getClassName() + "#" + name);
        }
        
        try {
            return match.invoke(null, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static ClassMirrorLoader makeChainedClassLoaderMirror(ClassMirrorLoader parent, ClassMirrorLoader child) {
        ClassMirror reflectionClassMirror = new NativeClassMirror(Reflection.class);
        ClassMirror classLoaderClass = new NativeClassMirror(ClassLoader.class);
        MethodMirror method = getStaticMethod(reflectionClassMirror, "makeChainedClassLoader", classLoaderClass, classLoaderClass);
        return (ClassMirrorLoader)mirrorInvoke(method, null, parent, child);
    }
    
    public static ChainedClassLoader makeChainedClassLoader(ClassLoader parent, ClassLoader child) {
        return new ChainedClassLoader(parent, child);
    }
    
    public static ClassMirror loadClassMirror(VirtualMachineMirror vm, ClassMirrorLoader originalLoader, String name) throws ClassNotFoundException {
        // String must be special-cased, because we can't call loadClass(String) to load String itself! We just make the
        // assumption that the VM defines the class, which is legitimate since the VM must also create string constants at the bytecode level.
        ClassMirror result;
        if (originalLoader == null || name.equals(String.class.getName()) || name.equals(getMirageBinaryClassName(String.class.getName(), false))) {
            result = vm.findBootstrapClassMirror(name);
        } else {
            ClassMirror stringClass = originalLoader.getClassMirror().getVM().findBootstrapClassMirror(String.class.getName());
            MethodMirror method;
            try {
                method = originalLoader.getClassMirror().getInstanceMethod("loadClass", stringClass);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            try {
                result = (ClassMirror)method.invoke((InstanceMirror)originalLoader, getMirror(name));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        if (result == null) {
            throw new ClassNotFoundException(name);
        }
        return result;
    }
    
    private static ClassMirror loadElementClassMirror(VirtualMachineMirror vm, Type elementType, boolean resolve, ClassMirrorLoader loader) throws ClassNotFoundException {
        if (elementType.equals(Type.BOOLEAN_TYPE)) {
            return new NativeClassMirror(Boolean.TYPE);
        } else if (elementType.equals(Type.BYTE_TYPE)) {
            return new NativeClassMirror(Byte.TYPE);
        } else if (elementType.equals(Type.CHAR_TYPE)) {
            return new NativeClassMirror(Character.TYPE);
        } else if (elementType.equals(Type.SHORT_TYPE)) {
            return new NativeClassMirror(Short.TYPE);
        } else if (elementType.equals(Type.INT_TYPE)) {
            return new NativeClassMirror(Integer.TYPE);
        } else if (elementType.equals(Type.LONG_TYPE)) {
            return new NativeClassMirror(Long.TYPE);
        } else if (elementType.equals(Type.FLOAT_TYPE)) {
            return new NativeClassMirror(Float.TYPE);
        } else if (elementType.equals(Type.DOUBLE_TYPE)) {
            return new NativeClassMirror(Double.TYPE);
        } else {
            return classMirrorForName(vm, elementType.getClassName(), resolve, loader);
        }
    }
    
    public static ClassMirror classMirrorForName(VirtualMachineMirror vm, String name, boolean resolve, ClassMirrorLoader loader) throws ClassNotFoundException {
        Type type = Type.getObjectType(name);
        if (type.getSort() == Type.ARRAY) {
            Type elementType = type.getElementType();
            ClassMirror elementClassMirror = loadElementClassMirror(vm, elementType, resolve, loader);
            return new ArrayClassMirror(loader, type.getDimensions(), elementClassMirror);
        } else {
            return Reflection.loadClassMirror(vm, loader, name);
        }
    }
    
    public static MethodMirror getInstanceMethod(ClassMirror klass, String name, ClassMirror... parameterTypes) {
        try {
            return klass.getInstanceMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            NoSuchMethodError error = new NoSuchMethodError(e.getMessage());
            error.initCause(e);
            throw error;
        }
    }
    
    public static MethodMirror getStaticMethod(ClassMirror klass, String name, ClassMirror... parameterTypes) {
        try {
            return klass.getStaticMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            NoSuchMethodError error = new NoSuchMethodError(e.getMessage());
            error.initCause(e);
            throw error;
        }
    }
    
    public static ObjectMirror mirrorInvoke(MethodMirror method, InstanceMirror obj, ObjectMirror... args) {
        try {
            return method.invoke(obj, args);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IllegalAccessException e) {
            IllegalAccessError error = new IllegalAccessError(e.getMessage());
            error.initCause(e);
            throw error;
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
}
