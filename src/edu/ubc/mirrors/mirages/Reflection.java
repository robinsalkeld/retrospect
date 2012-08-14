package edu.ubc.mirrors.mirages;

import static edu.ubc.mirrors.mirages.MirageClassGenerator.getMirageBinaryClassName;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;
import edu.ubc.mirrors.raw.NativeCharArrayMirror;
import edu.ubc.mirrors.raw.NativeInstanceMirror;
import edu.ubc.mirrors.raw.nativestubs.java.lang.SystemStubs;

public class Reflection {

    public static ObjectMirror getMirror(Object o) {
        if (o instanceof Mirage) {
            return ((Mirage)o).getMirror();
        } else {
            return NativeInstanceMirror.makeMirror(o);
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
    
    
    public static ClassMirror injectBytecode(VirtualMachineMirror vm, ThreadMirror thread, ClassMirrorLoader parent, ClassMirror newClass) {
        ClassMirror secureClassLoaderClass = vm.findBootstrapClassMirror(SecureClassLoader.class.getName());
        ClassMirror classLoaderClass = vm.findBootstrapClassMirror(ClassLoader.class.getName());
        ConstructorMirror constructor = getConstructor(secureClassLoaderClass, classLoaderClass);
        
        ClassMirrorLoader newLoader = (ClassMirrorLoader)newInstance(constructor, parent);
        
        ClassMirror stringClass = vm.findBootstrapClassMirror(String.class.getName());
        ClassMirror byteArrayClass = loadClassMirrorInternal(vm, null, "[B");
        ClassMirror intClass = vm.getPrimitiveClass("int");
        MethodMirror defineClassMethod = getMethod(classLoaderClass, "defineClass", stringClass, byteArrayClass, intClass, intClass);
        defineClassMethod.setAccessible(true);
        
        InstanceMirror name = makeString(vm, newClass.getClassName());
        byte[] bytecode = newClass.getBytecode();
        ObjectMirror bytecodeInVM = copyArray(vm, new NativeByteArrayMirror(bytecode));
        
        return (ClassMirror)mirrorInvoke(thread, defineClassMethod, newLoader, name, bytecodeInVM, 0, bytecode.length);
    }
    
    public static InstanceMirror makeString(VirtualMachineMirror vm, String value) {
        if (value == null) {
            return null;
        }
        
        ClassMirror stringClass = vm.findBootstrapClassMirror(String.class.getName());
        ObjectMirror chars = copyArray(vm, new NativeCharArrayMirror(value.toCharArray()));
        ConstructorMirror constructor = getConstructor(stringClass, chars.getClassMirror());
        return newInstance(constructor, chars);
    }
    
    public static String getRealStringForMirror(InstanceMirror mirror) {
        if (mirror == null) {
            return null;
        }
        
        if (!mirror.getClassMirror().getClassName().equals(String.class.getName())) {
            throw new IllegalArgumentException("Wrong class: " + mirror.getClassMirror());
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
        if (otherValue == null) {
            return null;
        }
        
        ClassMirror targetClass = loadClassMirrorInternal(vm, null, otherValue.getClassMirror().getClassName()).getComponentClassMirror();
        ArrayMirror target = targetClass.newArray(otherValue.length());
        SystemStubs.arraycopyMirrors(otherValue, 0, target, 0, otherValue.length());
        return target;
    }
    
    public static ObjectMirror deepcopy(VirtualMachineMirror vm, ObjectMirror mirror) {
        return deepcopy(vm, mirror, new HashMap<ObjectMirror, ObjectMirror>());
    }
    
    public static ObjectMirror deepcopy(VirtualMachineMirror vm, ObjectMirror mirror, Map<ObjectMirror, ObjectMirror> copies) {
        if (mirror == null) {
            return null;
        }
        
        ObjectMirror result = copies.get(mirror);
        if (result != null) {
            return result;
        }
        
        if (mirror instanceof ClassMirror) {
            ClassMirror src = (ClassMirror)mirror;
            if (src.getLoader() != null) {
                throw new IllegalArgumentException("Class mirror " + src + " is not a bootstrap class");
            }
            result = loadClassMirrorInternal(vm, null, src.getClassName());
        } else {
            ClassMirror targetClass = (ClassMirror)deepcopy(vm, mirror.getClassMirror(), copies);
            
            if (mirror instanceof ArrayMirror) {
                ArrayMirror src = (ArrayMirror)mirror;
                int length = src.length();
                ArrayMirror dest = targetClass.getComponentClassMirror().newArray(src.length());
                result = dest;
                copies.put(mirror, result);
                
                if (targetClass.getComponentClassMirror().isPrimitive()) {
                    SystemStubs.arraycopyMirrors(src, 0, dest, 0, length);
                } else {
                    ObjectArrayMirror srcOA = (ObjectArrayMirror)src;
                    ObjectArrayMirror destOA = (ObjectArrayMirror)dest;
                    for (int i = 0; i < length; i++) {
                        destOA.set(i, deepcopy(vm, srcOA.get(i), copies));
                    }
                }
            } else if (mirror instanceof InstanceMirror) {
                InstanceMirror src = (InstanceMirror)mirror;
                InstanceMirror dest = targetClass.newRawInstance();
                result = dest;
                copies.put(mirror, result);
                
                for (FieldMirror field : src.getMemberFields()) {
                    ClassMirror fieldType = field.getType();
                    String fieldTypeName = fieldType.getClassName();
                    try {
                        FieldMirror resultField = dest.getMemberField(field.getName());
                        if (fieldTypeName.equals("boolean")) {
                            resultField.setBoolean(field.getBoolean());
                        } else if (fieldTypeName.equals("byte")) {
                            resultField.setByte(field.getByte());
                        } else if (fieldTypeName.equals("char")) {
                            resultField.setChar(field.getChar());
                        } else if (fieldTypeName.equals("short")) {
                            resultField.setShort(field.getShort());
                        } else if (fieldTypeName.equals("int")) {
                            resultField.setInt(field.getInt());
                        } else if (fieldTypeName.equals("long")) {
                            resultField.setLong(field.getLong());
                        } else if (fieldTypeName.equals("float")) {
                            resultField.setFloat(field.getFloat());
                        } else if (fieldTypeName.equals("double")) {
                            resultField.setDouble(field.getDouble());
                        } else {
                            resultField.set(deepcopy(vm, field.get(), copies));
                        }
                    } catch (NoSuchFieldException e) {
                        throw new NoSuchFieldError(e.getMessage());
                    } catch (IllegalAccessException e) {
                        throw new IllegalAccessError(e.getMessage());
                    }
                }
            }
        }
        
        return result;
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
            
            ThreadMirror thread = ClassHolograph.currentThreadMirror.get();
            
            result = (ClassMirror)mirrorInvoke(thread, method, (InstanceMirror)originalLoader, makeString(vm, name));
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
    
    public static Type typeForClassMirror(ClassMirror classMirror) {
        String name = classMirror.getClassName();
        if (classMirror.isPrimitive()) {
            if (name.equals("int")) {
                return Type.INT_TYPE;
            } else if (name.equals("void")) {
                return Type.VOID_TYPE;
            } else if (name.equals("boolean")) {
                return Type.BOOLEAN_TYPE;
            } else if (name.equals("byte")) {
                return Type.BYTE_TYPE;
            } else if (name.equals("char")) {
                return Type.CHAR_TYPE;
            } else if (name.equals("short")) {
                return Type.SHORT_TYPE;
            } else if (name.equals("double")) {
                return Type.DOUBLE_TYPE;
            } else if (name.equals("float")) {
                return Type.FLOAT_TYPE;
            } else /* if (name.equals("long")) */{
                return Type.LONG_TYPE;
            }
        } else if (classMirror.isArray()) {
            return Type.getType(name);
        } else {
            return Type.getObjectType(name.replace('.', '/'));
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
    
    public static Object mirrorInvoke(ThreadMirror thread, MethodMirror method, ObjectMirror obj, Object... args) {
        try {
            return method.invoke(thread, obj, args);
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
        return loadClassMirrorInternal(context.getVM(), context.getLoader(), name);
    }
    
    public static ClassMirror loadClassMirrorInternal(VirtualMachineMirror vm, ClassMirrorLoader loader, String name) {
        try {
            return classMirrorForName(vm, name, false, loader);
        } catch (ClassNotFoundException e) {
            NoClassDefFoundError error = new NoClassDefFoundError(e.getMessage());
            error.initCause(e);
            throw error;
        }
    }
    
    public static ObjectMirror getField(InstanceMirror o, String name) {
        try {
            return o.getMemberField(name).get();
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
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
     
    public static void setField(InstanceMirror o, String name, int value) {
        try {
            o.getMemberField(name).setInt(value);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
    }
     
    public static ObjectArrayMirror toArray(ClassMirror elementType, Collection<? extends ObjectMirror> c) {
        ObjectArrayMirror result = (ObjectArrayMirror)elementType.newArray(c.size());
        int i = 0;
        for (ObjectMirror o : c) {
            result.set(i++, o);
        }
        return result;
    }
    
    public static ObjectMirror[] fromArray(ObjectArrayMirror mirror) {
        ObjectMirror[] result = new ObjectMirror[mirror.length()];
        for (int i = 0; i < result.length; i++) {
            result[i] = mirror.get(i);
        }
        return result;
    }
    
    public static String toString(ObjectMirror mirror) {
        VirtualMachineMirror vm = mirror.getClassMirror().getVM();
        ClassMirror vmObjectClass = vm.findBootstrapClassMirror(Object.class.getName());
        MethodMirror toStringMethod = getMethod(vmObjectClass, "toString");
        InstanceMirror stringMirror = (InstanceMirror)mirrorInvoke(vm.getThreads().get(0), toStringMethod, mirror);
        return getRealStringForMirror(stringMirror);
    }
    
    public static Object invokeMethodHandle(ThreadMirror thread, InstanceMirror obj, MethodHandle m, Object ... args) {
        ClassMirror klass = obj.getClassMirror();
        VirtualMachineMirror vm = klass.getVM();
        ClassMirror targetClass;
        try {
            targetClass = classMirrorForName(vm, m.getMethod().owner, false, klass.getLoader());
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getMessage());
        }
        Type[] paramTypes = Type.getArgumentTypes(m.getMethod().desc);
        ClassMirror[] paramClasses = new ClassMirror[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            try {
                paramClasses[i] = classMirrorForType(vm, paramTypes[i], false, klass.getLoader());
            } catch (ClassNotFoundException e) {
                throw new NoClassDefFoundError(e.getMessage());
            }
        }
        MethodMirror method = getMethod(targetClass, m.getMethod().name, paramClasses);
        return mirrorInvoke(thread, method, obj, args);
    }
    
    public static List<URL> getBootstrapPath() {
        String path = (String)System.getProperties().get("sun.boot.class.path");
        String[] paths = path.split(File.pathSeparator);
        URL[] urls = new URL[paths.length];
        for (int i = 0; i < paths.length; i++) {
            try {
                urls[i] = new File(paths[i]).toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return new ArrayList<URL>(Arrays.asList(urls));
    }
    
    public static Map<String, String> getStandardMappedFiles() {
        Map<String, String> mappedFiles = new HashMap<String, String>();
        
        String javaHome = (String)System.getProperties().get("java.home");
        mappedFiles.put(javaHome, javaHome);
        
        String eclipsePlugins = "/Library/Application Support/eclipse/plugins";
        mappedFiles.put(eclipsePlugins, eclipsePlugins);
        
        String extDir = "/System/Library/Java/Extensions/";
        mappedFiles.put(extDir, extDir);
        
        return mappedFiles;
    }

    public static boolean isInstance(ClassMirror classMirror, ObjectMirror oMirror) {
        return isAssignableFrom(classMirror, oMirror.getClassMirror());
    }
    
    public static FieldMirror getStaticField(ClassMirror klass, String fieldName) throws NoSuchFieldException {
        try {
            return klass.getStaticField(fieldName);
        } catch (NoSuchFieldException e) {
            // Continue
        }
        
        // TODO-RS: Check the spec on the ordering here
        ClassMirror superclass = klass.getSuperClassMirror();
        if (superclass != null) {
            try {
                return getStaticField(superclass, fieldName);
            } catch (NoSuchFieldException e) {
                // Ignore
            }
        }
        
        for (ClassMirror i : klass.getInterfaceMirrors()) {
            try {
                return getStaticField(i, fieldName);
            } catch (NoSuchFieldException e) {
                // Ignore
            }
        }
        
        throw new NoSuchFieldException(fieldName);
    }
}
