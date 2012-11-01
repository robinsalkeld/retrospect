package edu.ubc.mirrors.mirages;

import static edu.ubc.mirrors.mirages.MirageClassGenerator.getMirageBinaryClassName;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

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
import edu.ubc.mirrors.fieldmap.DirectArrayMirror;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;
import edu.ubc.mirrors.raw.NativeCharArrayMirror;
import edu.ubc.mirrors.raw.NativeInstanceMirror;
import edu.ubc.mirrors.raw.nativestubs.java.lang.SystemStubs;

public class Reflection {

    public static <T> T withThread(ThreadMirror t, Callable<T> c) throws Exception {
        ThreadHolograph threadHolograph = (ThreadHolograph)t;
        threadHolograph.enterHologramExecution();
        try {
            return c.call();
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            threadHolograph.exitHologramExecution();
        }
    }
    
    public static ObjectMirror getMirror(Object o) {
        if (o instanceof Mirage) {
            return ((Mirage)o).getMirror();
        } else {
            return NativeInstanceMirror.makeMirror(o);
        }
    }
    
    public static ClassMirror injectBytecode(VirtualMachineMirror vm, ThreadMirror thread, ClassMirrorLoader parent, ClassMirror newClass) {
        ClassMirror secureClassLoaderClass = vm.findBootstrapClassMirror(SecureClassLoader.class.getName());
        ClassMirror classLoaderClass = vm.findBootstrapClassMirror(ClassLoader.class.getName());
        ConstructorMirror constructor = HolographInternalUtils.getConstructor(secureClassLoaderClass, classLoaderClass);
        
        ClassMirrorLoader newLoader = (ClassMirrorLoader)HolographInternalUtils.newInstance(constructor, thread, parent);
        
        ClassMirror stringClass = vm.findBootstrapClassMirror(String.class.getName());
        ClassMirror byteArrayClass = HolographInternalUtils.loadClassMirrorInternal(vm, null, "[B");
        ClassMirror intClass = vm.getPrimitiveClass("int");
        MethodMirror defineClassMethod = HolographInternalUtils.getMethod(classLoaderClass, "defineClass", stringClass, byteArrayClass, intClass, intClass);
        defineClassMethod.setAccessible(true);
        
        InstanceMirror name = makeString(vm, newClass.getClassName());
        byte[] bytecode = newClass.getBytecode();
        ObjectMirror bytecodeInVM = copyArray(vm, new NativeByteArrayMirror(bytecode));
        
        return (ClassMirror)HolographInternalUtils.mirrorInvoke(thread, defineClassMethod, newLoader, name, bytecodeInVM, 0, bytecode.length);
    }
    
    public static ClassMirrorLoader newURLClassLoader(VirtualMachineMirror vm, ThreadMirror thread, ClassMirrorLoader parent, URL[] urls) {
        ThreadHolograph threadHolograph = (ThreadHolograph)thread;
        threadHolograph.enterHologramExecution();
        try {
            ClassMirror urlClass = vm.findBootstrapClassMirror(URL.class.getName());
            ConstructorMirror urlConstructor = HolographInternalUtils.getConstructor(urlClass, vm.findBootstrapClassMirror(String.class.getName()));
            
            ObjectArrayMirror urlsMirror = (ObjectArrayMirror)urlClass.newArray(urls.length);
            ClassMirror urlArrayClass = urlsMirror.getClassMirror();
            for (int i = 0; i < urls.length; i++) {
                InstanceMirror url = (InstanceMirror)HolographInternalUtils.newInstance(urlConstructor, thread, makeString(vm, urls[i].toString()));
                urlsMirror.set(i, url);
            }
            
            ClassMirror urlClassLoaderClass = vm.findBootstrapClassMirror(URLClassLoader.class.getName());
            ClassMirror classLoaderClass = vm.findBootstrapClassMirror(ClassLoader.class.getName());
            ConstructorMirror constructor = HolographInternalUtils.getConstructor(urlClassLoaderClass, urlArrayClass, classLoaderClass);
            
            return (ClassMirrorLoader)HolographInternalUtils.newInstance(constructor, thread, urlsMirror, parent);
        } finally {
            threadHolograph.exitHologramExecution();
        }
    }
    
    public static InstanceMirror makeString(VirtualMachineMirror vm, String s) {
        if (s == null) {
            return null;
        }
        
        ClassMirror stringClass = vm.findBootstrapClassMirror(String.class.getName());
        InstanceMirror result = stringClass.newRawInstance();
        ClassMirror charArrayClass = vm.getArrayClass(1, vm.getPrimitiveClass("char"));
        
        CharArrayMirror value = new DirectArrayMirror(charArrayClass, s.length());
        SystemStubs.arraycopyMirrors(new NativeCharArrayMirror(s.toCharArray()), 0, value, 0, s.length());
        try {
            stringClass.getDeclaredField("value").set(result, value);
            stringClass.getDeclaredField("count").setInt(result, s.length());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    
    public static String getRealStringForMirror(InstanceMirror mirror) {
        if (mirror == null) {
            return null;
        }
        
        ClassMirror stringClass = mirror.getClassMirror();
        if (!stringClass.getClassName().equals(String.class.getName())) {
            throw new IllegalArgumentException("Wrong class: " + stringClass);
        }
        
        try {
            CharArrayMirror valueMirror = (CharArrayMirror)stringClass.getDeclaredField("value").get(mirror);
            char[] value = new char[valueMirror.length()];
            NativeCharArrayMirror nativeValueMirror = new NativeCharArrayMirror(value);
            SystemStubs.arraycopyMirrors(valueMirror, 0, nativeValueMirror, 0, value.length);
            int offset = stringClass.getDeclaredField("offset").getInt(mirror);
            int count = stringClass.getDeclaredField("count").getInt(mirror);
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
        
        ClassMirror targetClass = HolographInternalUtils.loadClassMirrorInternal(vm, null, otherValue.getClassMirror().getClassName()).getComponentClassMirror();
        ArrayMirror target = targetClass.newArray(otherValue.length());
        SystemStubs.arraycopyMirrors(otherValue, 0, target, 0, otherValue.length());
        return target;
    }
    
    public static ClassMirror loadClassMirror(VirtualMachineMirror vm, ThreadMirror thread, ClassMirrorLoader originalLoader, String name) throws ClassNotFoundException {
        // String must be special-cased, because we can't call loadClass(String) to load String itself! We just make the
        // assumption that the VM defines the class, which is legitimate since the VM must also create string constants at the bytecode level.
        ClassMirror result;
        if (originalLoader == null || name.equals(String.class.getName()) || name.equals(getMirageBinaryClassName(String.class.getName(), false))) {
            result = vm.findBootstrapClassMirror(name);
        } else {
            ClassMirror stringClass = vm.findBootstrapClassMirror(String.class.getName());
            ClassMirror classLoaderClass = vm.findBootstrapClassMirror(ClassLoader.class.getName());
            MethodMirror method = HolographInternalUtils.getMethod(classLoaderClass, "loadClass", stringClass);
            
            result = (ClassMirror)HolographInternalUtils.mirrorInvoke(thread, method, (InstanceMirror)originalLoader, makeString(vm, name));
        }
        if (result == null) {
            throw new ClassNotFoundException(name);
        }
        return result;
    }
    
    public static ClassMirror classMirrorForType(VirtualMachineMirror vm, ThreadMirror thread, Type type, boolean resolve, ClassMirrorLoader loader) throws ClassNotFoundException {
        if (type.getSort() == Type.ARRAY) {
            Type elementType = type.getElementType();
            ClassMirror elementClassMirror = classMirrorForType(vm, thread, elementType, resolve, loader);
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
        } else if (type.equals(Type.VOID_TYPE)) {
            return vm.getPrimitiveClass("void");
        } else {
            return Reflection.loadClassMirror(vm, thread, loader, type.getClassName());
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
    
    public static ClassMirror classMirrorForName(VirtualMachineMirror vm, ThreadMirror thread, String name, boolean resolve, ClassMirrorLoader loader) throws ClassNotFoundException {
        return classMirrorForType(vm, thread, Type.getObjectType(name), resolve, loader);
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
        MethodMirror toStringMethod = HolographInternalUtils.getMethod(vmObjectClass, "toString");
        InstanceMirror stringMirror = (InstanceMirror)HolographInternalUtils.mirrorInvoke(vm.getThreads().get(0), toStringMethod, mirror);
        return getRealStringForMirror(stringMirror);
    }
    
    public static Object invokeMethodHandle(ObjectMirror obj, MethodHandle m, Object ... args) {
	ClassMirror klass = obj.getClassMirror();
        VirtualMachineMirror vm = klass.getVM();
        return m.invoke(obj, vm.getThreads().get(0), args);
    }
   
    public static Object invokeMethodHandle(ObjectMirror obj, ThreadMirror thread, MethodHandle m, Object ... args) {
        return m.invoke(obj, thread, args);
    }
    
    public static Object invokeStaticMethodHandle(ClassMirror targetClass, MethodHandle m, Object ... args) {
        VirtualMachineMirror vm = targetClass.getVM();
        ThreadMirror thread = vm.getThreads().get(0);
        Type[] paramTypes = Type.getArgumentTypes(m.getMethod().desc);
        ClassMirror[] paramClasses = new ClassMirror[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            try {
                paramClasses[i] = classMirrorForType(vm, thread, paramTypes[i], false, targetClass.getLoader());
            } catch (ClassNotFoundException e) {
                throw new NoClassDefFoundError(e.getMessage());
            }
        }
        MethodMirror method = HolographInternalUtils.getMethod(targetClass, m.getMethod().name, paramClasses);
        return HolographInternalUtils.mirrorInvoke(thread, method, null, args);
    }
    
    public static List<URL> getBootstrapPath() {
        String path = (String)System.getProperties().get("sun.boot.class.path");
        String[] paths = path.split(File.pathSeparator);
        URL[] urls = new URL[paths.length];
        for (int i = 0; i < paths.length; i++) {
            try {
                urls[i] = new File(paths[i]).toURI().toURL();
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
    
    public static FieldMirror getField(ClassMirror klass, String fieldName) throws NoSuchFieldException {
        try {
            return klass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // Continue
        }
        
        for (ClassMirror i : klass.getInterfaceMirrors()) {
            try {
                return getField(i, fieldName);
            } catch (NoSuchFieldException e) {
                // Ignore
            }
        }
        
        ClassMirror superclass = klass.getSuperClassMirror();
        if (superclass != null) {
            try {
                return getField(superclass, fieldName);
            } catch (NoSuchFieldException e) {
                // Ignore
            }
        }
        
        throw new NoSuchFieldException(fieldName);
    }

    public static <T> T checkNull(T t) {
        if (t == null) {
            throw new NullPointerException();
        }
        return t;
    }
    
    /**
     * Given a class name in the form "int[][]" or "a.b.c.D[]", returns
     * a class name in the form "[[I" or "[La.b.c.D;"
     */
    public static String arrayClassName(String name) {
        String elementName = name;
        String dimsString = "";
        while (elementName.endsWith("[]")) {
            elementName = elementName.substring(0, elementName.length() - 2);
            dimsString += "[";
        }
        return dimsString + arrayElementDescriptor(elementName);
    }
    
    public static String arrayElementDescriptor(String name) {
        if (name.equals("boolean")) {
            return "Z";
        } else if (name.equals("byte")) {
            return "B";
        } else if (name.equals("char")) {
            return "C";
        } else if (name.equals("short")) {
            return "S";
        } else if (name.equals("int")) {
            return "I";
        } else if (name.equals("long")) {
            return "J";
        } else if (name.equals("float")) {
            return "F";
        } else if (name.equals("double")) {
            return "D";
        } else {
            return "L" + name + ";";
        }
    }
    
    public static MethodMirror methodMirrorForMethodInstance(InstanceMirror m) {
	ClassMirror declaringClass = (ClassMirror)HolographInternalUtils.getField(m, "clazz");
        String name = Reflection.getRealStringForMirror((InstanceMirror)HolographInternalUtils.getField(m, "name"));
        ObjectArrayMirror parameterTypesMirror = (ObjectArrayMirror)HolographInternalUtils.getField(m, "parameterTypes");
        ClassMirror[] parameterTypes = new ClassMirror[parameterTypesMirror.length()];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = (ClassMirror)parameterTypesMirror.get(i);
        }

        try {
            return declaringClass.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }
    
    public static InstanceMirror newByteArrayPrintStream(VirtualMachineMirror vm) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException, NoSuchMethodException {
	ThreadMirror thread = vm.getThreads().get(0);
	InstanceMirror baos = vm.findBootstrapClassMirror(ByteArrayOutputStream.class.getName())
		.getConstructor().newInstance(thread);
	return vm.findBootstrapClassMirror(PrintStream.class.getName())
		.getConstructor(vm.findBootstrapClassMirror(OutputStream.class.getName())).newInstance(thread, baos);
	      
    }
    
    public static List<FieldMirror> getAllFields(ClassMirror klass) {
        List<FieldMirror> result = new ArrayList<FieldMirror>();
        while (klass != null) {
            result.addAll(klass.getDeclaredFields());
            klass = klass.getSuperClassMirror();
        }
        return result;
    }

    public static Object getBoxedValue(FieldMirror field, InstanceMirror instance) throws IllegalAccessException {
        Type fieldType = Reflection.typeForClassMirror(field.getType());
        switch (fieldType.getSort()) {
        case Type.BOOLEAN: return field.getBoolean(instance);
        case Type.BYTE: return field.getByte(instance);
        case Type.CHAR: return field.getChar(instance);
        case Type.SHORT: return field.getShort(instance);
        case Type.INT: return field.getInt(instance);
        case Type.LONG: return field.getLong(instance);
        case Type.FLOAT: return field.getFloat(instance);
        case Type.DOUBLE: return field.getDouble(instance);
        default: return field.get(instance);
        }
    }

    public static FieldMirror findField(ClassMirror klass, String name) throws NoSuchFieldException {
        while (klass != null) {
            try {
                return klass.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                klass = klass.getSuperClassMirror();
            }
        }
        throw new NoSuchFieldException(name);
    }
}
