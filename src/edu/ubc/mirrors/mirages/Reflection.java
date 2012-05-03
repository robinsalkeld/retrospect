package edu.ubc.mirrors.mirages;

import static edu.ubc.mirrors.mirages.MirageClassGenerator.getMirageBinaryClassName;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.SecureClassLoader;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.fieldmap.DirectArrayMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.raw.ArrayClassMirror;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;
import edu.ubc.mirrors.raw.NativeCharArrayMirror;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.raw.NativeObjectMirror;
import edu.ubc.mirrors.raw.nativestubs.java.lang.SystemStubs;
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
    
    
    public static ClassMirror injectBytecode(ClassMirrorLoader parent, ClassMirror newClass) {
        VirtualMachineMirror vm = parent.getClassMirror().getVM();
        
        ClassMirror secureClassLoaderClass = vm.findBootstrapClassMirror(SecureClassLoader.class.getName());
        ClassMirror classLoaderClass = vm.findBootstrapClassMirror(ClassLoader.class.getName());
        ConstructorMirror constructor = getConstructor(secureClassLoaderClass, classLoaderClass);
        constructor.setAccessible(true);
        ClassMirrorLoader newLoader = (ClassMirrorLoader)newInstance(constructor, parent);
        
        ClassMirror stringClass = vm.findBootstrapClassMirror(String.class.getName());
        ClassMirror byteArrayClass = loadClassMirrorInternal(stringClass, "[B");
        ClassMirror intClass = vm.getPrimitiveClass("int");
        MethodMirror defineClassMethod = getMethod(classLoaderClass, "defineClass", stringClass, byteArrayClass, intClass, intClass);
        defineClassMethod.setAccessible(true);
        
        InstanceMirror name = makeString(vm, newClass.getClassName());
        byte[] bytecode = newClass.getBytecode();
        ObjectMirror bytecodeInVM = copyArray(vm, new NativeByteArrayMirror(bytecode));
        
        return (ClassMirror)mirrorInvoke(defineClassMethod, newLoader, name, bytecodeInVM, 0, bytecode.length);
    }
    
    public static InstanceMirror makeString(VirtualMachineMirror vm, String value) {
        ClassMirror stringClass = vm.findBootstrapClassMirror(String.class.getName());
        ObjectMirror chars = copyArray(vm, new NativeCharArrayMirror(value.toCharArray()));
        ConstructorMirror constructor = getConstructor(stringClass, chars.getClassMirror());
        return newInstance(constructor, chars);
    }
    
    public static String getRealStringForMirror(InstanceMirror mirror) {
        if (mirror == null) {
            return null;
        }
        
        try {
            CharArrayMirror valueMirror = (CharArrayMirror)mirror.getMemberField("value").get();
            char[] value = new char[valueMirror.length()];
            NativeCharArrayMirror nativeValueMirror = new NativeCharArrayMirror(value);
            SystemStubs.arraycopyMirrors(valueMirror, 0, nativeValueMirror, 0, value.length);
            int offset = mirror.getMemberField("offset").getInt();
            int count = mirror.getMemberField("count").getInt();
            return new String(value, offset, count);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
    }
    
    public static ArrayMirror copyArray(VirtualMachineMirror vm, ArrayMirror otherValue) {
        ClassMirror targetClass;
        try {
            targetClass = classMirrorForName(vm, otherValue.getClassMirror().getClassName(), false, null).getComponentClassMirror();
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getMessage());
        }
        
        ArrayMirror target = targetClass.newArray(otherValue.length());
        SystemStubs.arraycopyMirrors(otherValue, 0, target, 0, otherValue.length());
        return target;
    }
    
    public static ClassMirror loadClassMirror(VirtualMachineMirror vm, ClassMirrorLoader originalLoader, String name) throws ClassNotFoundException {
        // String must be special-cased, because we can't call loadClass(String) to load String itself! We just make the
        // assumption that the VM defines the class, which is legitimate since the VM must also create string constants at the bytecode level.
        ClassMirror result;
        if (originalLoader == null || name.equals(String.class.getName()) || name.equals(getMirageBinaryClassName(String.class.getName(), false))) {
            result = vm.findBootstrapClassMirror(name);
        } else {
            ClassMirror stringClass = vm.findBootstrapClassMirror(String.class.getName());
            ClassMirror classLoaderClass = vm.findBootstrapClassMirror(ClassLoader.class.getName());
            MethodMirror method = getMethod(classLoaderClass, "loadClass", stringClass);
            result = (ClassMirror)mirrorInvoke(method, (InstanceMirror)originalLoader, makeString(vm, name));
        }
        if (result == null) {
            throw new ClassNotFoundException(name);
        }
        return result;
    }
    
    public static ClassMirror classMirrorForType(VirtualMachineMirror vm, Type type, boolean resolve, ClassMirrorLoader loader) throws ClassNotFoundException {
        if (type.getSort() == Type.ARRAY) {
            Type elementType = type.getElementType();
            ClassMirror elementClassMirror = classMirrorForType(vm, elementType, resolve, loader);
            return vm.getArrayClass(type.getDimensions(), elementClassMirror);
        } else if (type.equals(Type.BOOLEAN_TYPE)) {
            return vm.getPrimitiveClass("boolean");
        } else if (type.equals(Type.BYTE_TYPE)) {
            return vm.getPrimitiveClass("byte");
        } else if (type.equals(Type.CHAR_TYPE)) {
            return vm.getPrimitiveClass("char");
        } else if (type.equals(Type.SHORT_TYPE)) {
            return vm.getPrimitiveClass("short");
        } else if (type.equals(Type.INT_TYPE)) {
            return vm.getPrimitiveClass("int");
        } else if (type.equals(Type.LONG_TYPE)) {
            return vm.getPrimitiveClass("long");
        } else if (type.equals(Type.FLOAT_TYPE)) {
            return vm.getPrimitiveClass("float");
        } else if (type.equals(Type.DOUBLE_TYPE)) {
            return vm.getPrimitiveClass("double");
        } else {
            return Reflection.loadClassMirror(vm, loader, type.getClassName());
        }
    }
    
    public static ClassMirror classMirrorForName(VirtualMachineMirror vm, String name, boolean resolve, ClassMirrorLoader loader) throws ClassNotFoundException {
        return classMirrorForType(vm, Type.getObjectType(name), resolve, loader);
    }
    
    public static MethodMirror getMethod(ClassMirror klass, String name, ClassMirror... parameterTypes) {
        try {
            return klass.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            NoSuchMethodError error = new NoSuchMethodError(e.getMessage());
            error.initCause(e);
            throw error;
        }
    }
    
    public static ConstructorMirror getConstructor(ClassMirror klass, ClassMirror... parameterTypes) {
        try {
            return klass.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            NoSuchMethodError error = new NoSuchMethodError(e.getMessage());
            error.initCause(e);
            throw error;
        }
    }
    
    public static Object mirrorInvoke(MethodMirror method, InstanceMirror obj, Object... args) {
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
    
    public static InstanceMirror newInstance(ConstructorMirror constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (IllegalAccessException e) {
            IllegalAccessError error = new IllegalAccessError(e.getMessage());
            error.initCause(e);
            throw error;
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            InstantiationError error = new InstantiationError(e.getMessage());
            error.initCause(e);
            throw error;
        }
    }
    
    public static boolean isAssignableFrom(ClassMirror thiz, ClassMirror other) {
        if (thiz.equals(other)) {
            return true;
        }
        
        if (thiz.isArray()) {
            return other.isArray() && isAssignableFrom(thiz.getComponentClassMirror(), other.getComponentClassMirror());
        }
        
        if (other.isInterface() && thiz.getClassName().equals(Object.class.getName())) {
            return true; 
        }
        ClassMirror otherSuperclass = other.getSuperClassMirror();
        if (otherSuperclass != null && isAssignableFrom(thiz, otherSuperclass)) {
            return true;
        }

        for (ClassMirror interfaceNode : other.getInterfaceMirrors()) {
            if (isAssignableFrom(thiz, interfaceNode)) {
                return true;
            }
        }
        
        return false;
    }
    
    public static ClassMirror loadClassMirrorInternal(ClassMirror context, String name) {
        try {
            return classMirrorForName(context.getVM(), name, false, context.getLoader());
        } catch (ClassNotFoundException e) {
            NoClassDefFoundError error = new NoClassDefFoundError(e.getMessage());
            error.initCause(e);
            throw error;
        }
    }
    
    public static void setField(InstanceMirror o, String name, ObjectMirror value) {
        try {
            o.getMemberField(name).set(value);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
    }
}
