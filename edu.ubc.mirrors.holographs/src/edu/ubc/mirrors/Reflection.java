/*******************************************************************************
 * Copyright (c) 2013 Robin Salkeld
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package edu.ubc.mirrors;

import static edu.ubc.mirrors.holograms.HologramClassGenerator.getHologramBinaryClassName;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;
import edu.ubc.mirrors.raw.NativeCharArrayMirror;
import edu.ubc.mirrors.raw.NativeClassMirror;

public class Reflection {

    
    /*
     * "Binary class name":
     * java.lang.Object
     * int[]
     * java.lang.Object[]
     * 
     * "Internal class name" (non-array classes only):
     * java/lang/Object
     * 
     * "Descriptor":
     * Ljava/lang/Object;
     * [I
     * [java/lang/Object;
     */
    
    public static <T> T withThread(ThreadMirror t, Callable<T> c) {
        if (t instanceof ThreadHolograph) {
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
        } else {
            try {
                return c.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    // Stub method for testing
    public static <T> T withoutThread(ThreadMirror t, Callable<T> c) {
        try {
            return c.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static <T> T proxyWithThread(Class<T> interfaceClass, final ThreadMirror thread, final T original) {
        return interfaceClass.cast(Proxy.newProxyInstance(original.getClass().getClassLoader(), new Class<?>[] {interfaceClass}, new InvocationHandler() {
            public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
                if (ThreadHolograph.currentThreadMirror.get() == null) {
                    return withThread(thread, new Callable<Object>() {
                        public Object call() throws Exception {
                            return method.invoke(original, args);
                        }
                    });
                } else {
                    return method.invoke(original, args);
                }
            }
        }));
    }
    
    public static ClassMirror injectBytecode(final VirtualMachineMirror vm, final ThreadMirror thread, final ClassMirrorLoader parent, final String name, final byte[] newClassBytecode) {
        return Reflection.withThread(thread, new Callable<ClassMirror>() {
            public ClassMirror call() throws Exception {
                ClassMirror secureClassLoaderClass = vm.findBootstrapClassMirror(SecureClassLoader.class.getName());
                ClassMirror classLoaderClass = vm.findBootstrapClassMirror(ClassLoader.class.getName());
                ConstructorMirror constructor = HolographInternalUtils.getConstructor(secureClassLoaderClass, ClassLoader.class.getName());
                
                ClassMirrorLoader newLoader = (ClassMirrorLoader)HolographInternalUtils.newInstance(constructor, thread, parent);
                
                MethodMirror defineClassMethod = HolographInternalUtils.getMethod(classLoaderClass, "defineClass", String.class.getName(), "byte[]", "int", "int");
                defineClassMethod.setAccessible(true);
                
                InstanceMirror nameMirror = vm.makeString(name);
                ObjectMirror bytecodeInVM = copyArray(vm, new NativeByteArrayMirror(newClassBytecode));
                
                return (ClassMirror)HolographInternalUtils.mirrorInvoke(thread, defineClassMethod, newLoader, nameMirror, bytecodeInVM, 0, newClassBytecode.length);
                
            };
        });
    }
    
    public static ClassMirrorLoader newURLClassLoader(VirtualMachineMirror vm, ThreadMirror thread, ClassMirrorLoader parent, URL[] urls) {
//        ThreadHolograph threadHolograph = (ThreadHolograph)thread;
//        threadHolograph.enterHologramExecution();
//        try {
            ClassMirror urlClass = vm.findBootstrapClassMirror(URL.class.getName());
            ConstructorMirror urlConstructor = HolographInternalUtils.getConstructor(urlClass, String.class.getName());
            
            ObjectArrayMirror urlsMirror = (ObjectArrayMirror)urlClass.newArray(urls.length);
            for (int i = 0; i < urls.length; i++) {
                InstanceMirror url = (InstanceMirror)HolographInternalUtils.newInstance(urlConstructor, thread, vm.makeString(urls[i].toString()));
                urlsMirror.set(i, url);
            }
            
            ClassMirror urlClassLoaderClass = vm.findBootstrapClassMirror(URLClassLoader.class.getName());
            ConstructorMirror constructor = HolographInternalUtils.getConstructor(urlClassLoaderClass, urlsMirror.getClassMirror().getClassName(), ClassLoader.class.getName());
            
            return (ClassMirrorLoader)HolographInternalUtils.newInstance(constructor, thread, urlsMirror, parent);
//        } finally {
//            threadHolograph.exitHologramExecution();
//        }
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
            CharArrayMirror valueMirror = (CharArrayMirror)mirror.get(stringClass.getDeclaredField("value"));
            char[] value = new char[valueMirror.length()];
            NativeCharArrayMirror nativeValueMirror = new NativeCharArrayMirror(value);
            arraycopy(valueMirror, 0, nativeValueMirror, 0, value.length);
            FieldMirror offsetField = stringClass.getDeclaredField("offset");
            if (offsetField == null) {
                // Post Java 7 u5 - no more sharing of string storage
                return new String(value);
            } else {
                int offset = mirror.getInt(offsetField);
                int count = mirror.getInt(stringClass.getDeclaredField("count"));
                return new String(value, offset, count);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
    
    public static ArrayMirror copyArray(VirtualMachineMirror vm, ArrayMirror otherValue) {
        if (otherValue == null) {
            return null;
        }
        
        ClassMirror targetClass = HolographInternalUtils.loadClassMirrorInternal(vm, null, otherValue.getClassMirror().getClassName()).getComponentClassMirror();
        ArrayMirror target = targetClass.newArray(otherValue.length());
        arraycopy(otherValue, 0, target, 0, otherValue.length());
        return target;
    }
    
    public static ClassMirror loadClassMirror(VirtualMachineMirror vm, ThreadMirror thread, ClassMirrorLoader originalLoader, boolean resolve, String name) throws ClassNotFoundException, MirrorInvocationTargetException {
        // String must be special-cased, because we can't call loadClass(String) to load String itself! We just make the
        // assumption that the VM defines the class, which is legitimate since the VM must also create string constants at the bytecode level.
        ClassMirror result;
        if (originalLoader == null || name.equals(String.class.getName()) || name.equals(getHologramBinaryClassName(String.class.getName(), false))) {
            result = vm.findBootstrapClassMirror(name);
        } else {
            final ClassMirror classLoaderClass = vm.findBootstrapClassMirror(ClassLoader.class.getName());
            MethodMirror method = Reflection.withoutThread(thread, new Callable<MethodMirror>() {
                public MethodMirror call() throws Exception {
                    return HolographInternalUtils.getMethod(classLoaderClass, "loadClass", String.class.getName(), "boolean");
                }
            });
            method.setAccessible(true);
            
            ThreadHolograph.raiseMetalevel();
            try {
                result = (ClassMirror) method.invoke(thread,
                        (InstanceMirror) originalLoader, vm.makeString(name), resolve);
            } catch (IllegalAccessException e) {
                // Can't happen - can't reduce visibility of ClassLoader.loadClass()
                throw new RuntimeException(e);
            }
            ThreadHolograph.lowerMetalevel();
        }
        if (result == null) {
            throw new ClassNotFoundException(name);
        }
        
        if (false && resolve) {
            final ClassMirror classClass = vm.findBootstrapClassMirror(Class.class.getName());
            MethodMirror method = Reflection.withoutThread(thread, new Callable<MethodMirror>() {
                public MethodMirror call() throws Exception {
                    return HolographInternalUtils.getMethod(classClass, "getMethods");
                }
            });
            try {
                method.invoke(thread, result);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        
        return result;
    }
    
    public static ClassMirror classMirrorForType(VirtualMachineMirror vm, ThreadMirror thread, Type type, boolean resolve, ClassMirrorLoader loader) throws ClassNotFoundException, MirrorInvocationTargetException {
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
            return Reflection.loadClassMirror(vm, thread, loader, resolve, type.getClassName());
        }
    }
    
    /**
     * @param name A binary class name
     * @return
     */
    public static Type typeForClassName(String name) {
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
        } else if (name.equals("long")) {
            return Type.LONG_TYPE;
        } else {
            return Type.getType(arrayClassName(name).replace('.', '/'));
        }
    }
    
    public static Type typeForClassMirror(ClassMirror classMirror) {
        String name = classMirror.getClassName();
        if (classMirror.isPrimitive()) {
            return typeForClassName(name);
        } else {
            return Type.getType(arrayClassName(name).replace('.', '/'));
        }
    }
    
    /**
     * @param vm
     * @param thread
     * @param name Binary name of the class to load
     * @param resolve
     * @param loader
     * @return
     * @throws ClassNotFoundException
     * @throws MirrorInvocationTargetException
     */
    public static ClassMirror classMirrorForName(VirtualMachineMirror vm, ThreadMirror thread, String name, boolean resolve, ClassMirrorLoader loader) throws ClassNotFoundException, MirrorInvocationTargetException {
        return classMirrorForType(vm, thread, Reflection.typeForClassName(name), resolve, loader);
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
    
    public static ObjectArrayMirror toArray(ClassMirror elementType, ObjectMirror... objs) {
        return toArray(elementType, Arrays.asList(objs));
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
    
    public static Map<ObjectMirror, ObjectMirror> mapEntries(ObjectMirror map) {
        InstanceMirror /* entry set */ entrySet = (InstanceMirror)Reflection.invokeMethodHandle(map, 
                new MethodHandle() {
                    protected void methodCall() throws Throwable {
                        ((Map<?, ?>)null).entrySet();
                    }
                });
        Map<ObjectMirror, ObjectMirror> entries = new HashMap<ObjectMirror, ObjectMirror>();
        for (ObjectMirror entry : Reflection.collectionValues(entrySet)) {
            ObjectMirror key = (ObjectMirror)Reflection.invokeMethodHandle(entry, new MethodHandle() {
                protected void methodCall() throws Throwable {
                    ((Map.Entry<?, ?>)null).getKey();
                }
            });
            ObjectMirror value = (ObjectMirror)Reflection.invokeMethodHandle(entry, new MethodHandle() {
                protected void methodCall() throws Throwable {
                    ((Map.Entry<?, ?>)null).getValue();
                }
            });
            entries.put(key, value);
        }
        return entries;
    }
    
    public static List<ObjectMirror> collectionValues(ObjectMirror collection) {
        ObjectMirror iterator = (ObjectMirror)Reflection.invokeMethodHandle(collection, new MethodHandle() {
            @Override
            protected void methodCall() throws Throwable {
                ((Collection<?>)null).iterator();
            }
        });
        MethodHandle nextMethod = new MethodHandle() {
            @Override
            protected void methodCall() throws Throwable {
                ((Iterator<?>)null).next();
            }  
        };
        MethodHandle hasNextMethod = new MethodHandle() {
            @Override
            protected void methodCall() throws Throwable {
                ((Iterator<?>)null).hasNext();
            }  
        };
        List<ObjectMirror> result = new ArrayList<ObjectMirror>();
        while (((Boolean)Reflection.invokeMethodHandle(iterator, hasNextMethod)).booleanValue()) {
            ObjectMirror element = (ObjectMirror)Reflection.invokeMethodHandle(iterator, nextMethod);
            result.add(element);
        }
        return result;
    }
    
    private static final Map<VirtualMachineMirror, MethodMirror> TO_STRING_METHODS =
                 new HashMap<VirtualMachineMirror, MethodMirror>();
    
    public static String toString(ObjectMirror mirror) throws MirrorInvocationTargetException {
        if (mirror == null) {
            return "null";
        }
        
        return toString(mirror, mirror.getClassMirror().getVM().getThreads().get(0));
    }    
    
    public static String toString(ObjectMirror mirror, ThreadMirror thread) throws MirrorInvocationTargetException {
        if (mirror == null) {
            return "null";
        }
        
        VirtualMachineMirror vm = mirror.getClassMirror().getVM();
        MethodMirror toStringMethod = TO_STRING_METHODS.get(vm);
        if (toStringMethod == null) {
            ClassMirror vmObjectClass = vm.findBootstrapClassMirror(Object.class.getName());
            toStringMethod = HolographInternalUtils.getMethod(vmObjectClass, "toString");
            TO_STRING_METHODS.put(vm, toStringMethod);
        }
        try {
            InstanceMirror stringMirror = (InstanceMirror) toStringMethod
                    .invoke(thread, mirror);
            return getRealStringForMirror(stringMirror);
        } catch (IllegalAccessException e) {
            // Should never happen - can't reduce the visibility of Object.toString()
            throw new RuntimeException(e);
        }
    }
    
    public static Object invokeMethodHandle(ObjectMirror obj, MethodHandle m, Object ... args) {
	ClassMirror klass = obj.getClassMirror();
        VirtualMachineMirror vm = klass.getVM();
        return m.invoke(obj, vm.getThreads().get(0), args);
    }
   
    public static Object invokeMethodHandle(ObjectMirror obj, ThreadMirror thread, MethodHandle m, Object ... args) {
        return m.invoke(obj, thread, args);
    }
    
    public static Object invokeStaticMethodHandle(ThreadMirror thread, ClassMirror targetClass, MethodHandle m, Object ... args) throws IllegalAccessException, MirrorInvocationTargetException {
        MethodMirror method;
        try {
            method = getDeclaredMethod(targetClass, m.getMethod().name, Type.getType(m.getMethod().desc));
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } 
        return method.invoke(thread, null, args);
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
    
    public static FieldMirror getField(ClassMirror klass, String fieldName) {
        FieldMirror result = klass.getDeclaredField(fieldName);
        if (result != null) {
            return result;
        }
        
        for (ClassMirror i : klass.getInterfaceMirrors()) {
            result = getField(i, fieldName);
            if (result != null) {
                return result;
            }
        }
        
        ClassMirror superclass = klass.getSuperClassMirror();
        if (superclass != null) {
            result = getField(superclass, fieldName);
            if (result != null) {
                return result;
            }
        }
        
        return null;
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
    
    public static ConstructorMirror constructorMirrorForConstructorInstance(InstanceMirror constructor) {
        ClassMirror declaringClass = (ClassMirror)HolographInternalUtils.getField(constructor, "clazz");
        ObjectArrayMirror parameterTypesMirror = (ObjectArrayMirror)HolographInternalUtils.getField(constructor, "parameterTypes");
        String[] parameterTypeNames = new String[parameterTypesMirror.length()];
        for (int i = 0; i < parameterTypeNames.length; i++) {
            parameterTypeNames[i] = ((ClassMirror)parameterTypesMirror.get(i)).getClassName();
        }

        try {
            return declaringClass.getConstructor(parameterTypeNames);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }
    
    public static MethodMirror methodMirrorForMethodInstance(InstanceMirror m) {
        ClassMirror declaringClass = (ClassMirror)HolographInternalUtils.getField(m, "clazz");
        String name = Reflection.getRealStringForMirror((InstanceMirror)HolographInternalUtils.getField(m, "name"));
        ObjectArrayMirror parameterTypesMirror = (ObjectArrayMirror)HolographInternalUtils.getField(m, "parameterTypes");
        String[] parameterTypeNames = new String[parameterTypesMirror.length()];
        for (int i = 0; i < parameterTypeNames.length; i++) {
            parameterTypeNames[i] = ((ClassMirror)parameterTypesMirror.get(i)).getClassName();
        }

        try {
            return declaringClass.getDeclaredMethod(name, parameterTypeNames);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }
    
    public static InstanceMirror methodInstanceForMethodMirror(ThreadMirror thread, MethodMirror m) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, MirrorInvocationTargetException, ArrayIndexOutOfBoundsException, ClassNotFoundException {
        VirtualMachineMirror vm = m.getDeclaringClass().getVM();
        ClassMirror classClass = vm.findBootstrapClassMirror(Class.class.getName());
        MethodMirror getDeclaredMethodMethod = classClass.getDeclaredMethod("getDeclaredMethod", String.class.getName(), "java.lang.Class[]");
        
        int numParams = m.getParameterTypeNames().size();
        ObjectArrayMirror paramTypeArray = (ObjectArrayMirror)classClass.newArray(numParams);
        for (int i = 0; i < numParams; i++) {
            paramTypeArray.set(i, Reflection.classMirrorForName(vm, thread, m.getParameterTypeNames().get(i), true, m.getDeclaringClass().getLoader()));
        }
        
        Object[] args = new Object[2];
        args[0] = vm.makeString(m.getName());
        args[1] = paramTypeArray;
        
        return (InstanceMirror)getDeclaredMethodMethod.invoke(thread, m.getDeclaringClass(), args);
    }
    
    public static InstanceMirror constructorInstanceForConstructorMirror(ThreadMirror thread, ConstructorMirror c) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, MirrorInvocationTargetException, ArrayIndexOutOfBoundsException, ClassNotFoundException {
        VirtualMachineMirror vm = c.getDeclaringClass().getVM();
        ClassMirror classClass = vm.findBootstrapClassMirror(Class.class.getName());
        MethodMirror getDeclaredMethodMethod = classClass.getDeclaredMethod("getDeclaredMethod", String.class.getName(), "java.lang.Class[]");
        
        int numParams = c.getParameterTypeNames().size();
        ObjectArrayMirror paramTypeArray = (ObjectArrayMirror)classClass.newArray(numParams);
        for (int i = 0; i < numParams; i++) {
            paramTypeArray.set(i, Reflection.classMirrorForName(vm, thread, c.getParameterTypeNames().get(i), true, c.getDeclaringClass().getLoader()));
        }
        
        Object[] args = new Object[1];
        args[1] = paramTypeArray;
        
        return (InstanceMirror)getDeclaredMethodMethod.invoke(thread, c.getDeclaringClass(), args);
    }
    
    public static InstanceMirror newByteArrayPrintStream(VirtualMachineMirror vm) throws InstantiationException, IllegalAccessException, IllegalArgumentException, MirrorInvocationTargetException, SecurityException, NoSuchMethodException {
	ThreadMirror thread = vm.getThreads().get(0);
	InstanceMirror baos = vm.findBootstrapClassMirror(ByteArrayOutputStream.class.getName())
		.getConstructor().newInstance(thread);
	return vm.findBootstrapClassMirror(PrintStream.class.getName())
		.getConstructor(OutputStream.class.getName()).newInstance(thread, baos);
	      
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
        case Type.BOOLEAN: return instance.getBoolean(field);
        case Type.BYTE: return instance.getByte(field);
        case Type.CHAR: return instance.getChar(field);
        case Type.SHORT: return instance.getShort(field);
        case Type.INT: return instance.getInt(field);
        case Type.LONG: return instance.getLong(field);
        case Type.FLOAT: return instance.getFloat(field);
        case Type.DOUBLE: return instance.getDouble(field);
        default: return instance.get(field);
        }
    }

    public static FieldMirror findField(ClassMirror klass, String name) throws NoSuchFieldException {
        while (klass != null) {
            FieldMirror result = klass.getDeclaredField(name);
            if (result != null) {
                return result;
            }
            klass = klass.getSuperClassMirror();
        }
        return null;
    }
    
    public static void arraycopy(ObjectMirror src, int srcPos, ObjectMirror dest, int destPos, int length) {
        String signature = src.getClassMirror().getSignature();
        if (signature.equals("[B")) {
            if (!dest.getClassMirror().getSignature().equals("[B")) {
                throw new ArrayStoreException();
            }
            byte[] values = ((ByteArrayMirror)src).getBytes(srcPos, length);
            ((ByteArrayMirror)dest).setBytes(destPos, values);
            return;
        }
        
        for (int off = 0; off < length; off++) {
            setArrayElement(dest, destPos + off, getArrayElement(src, srcPos + off));
        }
    }
    
    public static Object getArrayElement(ObjectMirror am, int index) {
        String signature = am.getClassMirror().getSignature();
        if (signature.equals("[Z")) {
            return ((BooleanArrayMirror)am).getBoolean(index);
        } else if (signature.equals("[B")) {
            return ((ByteArrayMirror)am).getByte(index);
        } else if (signature.equals("[C")) {
            return ((CharArrayMirror)am).getChar(index);
        } else if (signature.equals("[S")) {
            return ((ShortArrayMirror)am).getShort(index);
        } else if (signature.equals("[I")) {
            return ((IntArrayMirror)am).getInt(index);
        } else if (signature.equals("[J")) {
            return ((LongArrayMirror)am).getLong(index);
        } else if (signature.equals("[F")) {
            return ((FloatArrayMirror)am).getFloat(index);
        } else if (signature.equals("[D")) {
            return ((DoubleArrayMirror)am).getDouble(index);
        } else {
            return ((ObjectArrayMirror)am).get(index);
        }
    }
    
    public static void setArrayElement(ObjectMirror am, int index, Object o) {
        String signature = am.getClassMirror().getSignature();
        if (signature.equals("[Z")) {
            ((BooleanArrayMirror)am).setBoolean(index, (Boolean)o);
        } else if (signature.equals("[B")) {
            ((ByteArrayMirror)am).setByte(index, (Byte)o);
        } else if (signature.equals("[C")) {
            ((CharArrayMirror)am).setChar(index, (Character)o);
        } else if (signature.equals("[S")) {
            ((ShortArrayMirror)am).setShort(index, (Short)o);
        } else if (signature.equals("[I")) {
            ((IntArrayMirror)am).setInt(index, (Integer)o);
        } else if (signature.equals("[J")) {
            ((LongArrayMirror)am).setLong(index, (Long)o);
        } else if (signature.equals("[F")) {
            ((FloatArrayMirror)am).setFloat(index, (Float)o);
        } else if (signature.equals("[D")) {
            ((DoubleArrayMirror)am).setDouble(index, (Double)o);
        } else {
            ((ObjectArrayMirror)am).set(index, (ObjectMirror)o);
        }
    }
    
    // Returns the ObjectMirror subclass (or primitive class) that represents the given type.
    public static Type getMirrorType(Type type) {
        switch (type.getSort()) {
        case Type.OBJECT: {
            String internalName = type.getInternalName();
            if (internalName.equals("java/lang/Object")) {
                return Type.getType(ObjectMirror.class);
            } else if (internalName.equals("java/lang/Class")) {
                return Type.getType(ClassMirror.class);
            } else if (internalName.equals("java/lang/ClassLoader")) {
                return Type.getType(ClassMirrorLoader.class);
            } else if (internalName.equals("java/lang/Thread")) {
                return Type.getType(ThreadMirror.class);
            } else {
                return Type.getType(InstanceMirror.class);
            }
        }
        case Type.ARRAY: {
                if (type.getDimensions() > 1) {
                    return Type.getType(ObjectArrayMirror.class);
                } else {
                    switch (type.getElementType().getSort()) {
                    case Type.BOOLEAN: return Type.getType(BooleanArrayMirror.class);
                    case Type.BYTE: return Type.getType(ByteArrayMirror.class);
                    case Type.CHAR: return Type.getType(CharArrayMirror.class);
                    case Type.SHORT: return Type.getType(ShortArrayMirror.class);
                    case Type.INT: return Type.getType(IntArrayMirror.class);
                    case Type.LONG: return Type.getType(LongArrayMirror.class);
                    case Type.FLOAT: return Type.getType(FloatArrayMirror.class);
                    case Type.DOUBLE: return Type.getType(DoubleArrayMirror.class);
                    default: return Type.getType(ObjectArrayMirror.class);
                    }
                }
            }
        default: return type;
        }
    }
    
    public static String getThreadName(ThreadMirror thread) {
        try {
            FieldMirror nameField = thread.getClassMirror().getVM().findBootstrapClassMirror(Thread.class.getName()).getDeclaredField("name");
            CharArrayMirror nameChars = (CharArrayMirror)thread.get(nameField);
            char[] chars = new char[nameChars.length()];
            Reflection.arraycopy(nameChars, 0, new NativeCharArrayMirror(chars), 0, chars.length);
            return new String(chars);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } 
    }
    
    public static Type getMethodType(MethodMirror method) {
        List<String> parameterTypeNames = method.getParameterTypeNames();
        Type[] argTypes = new Type[parameterTypeNames.size()];
        int i = 0;
        for (String paramTypeName : parameterTypeNames) {
            argTypes[i++] = typeForClassName(paramTypeName);
        }
        return Type.getMethodType(typeForClassName(method.getReturnTypeName()), argTypes);
    }
    
    public static Type getMethodType(ConstructorMirror constructor) {
        List<String> parameterTypeNames = constructor.getParameterTypeNames();
        Type[] argTypes = new Type[parameterTypeNames.size()];
        int i = 0;
        for (String paramTypeName : parameterTypeNames) {
            argTypes[i++] = typeForClassName(paramTypeName);
        }
        return Type.getMethodType(Type.VOID_TYPE, argTypes);
    }
    
    public static MethodMirror getDeclaredMethod(ClassMirror klass, String methodName, Type methodType) throws SecurityException, NoSuchMethodException {
        Type[] argumentTypes = methodType.getArgumentTypes();
        String[] paramClassNames = new String[argumentTypes.length];
        for (int i = 0; i < argumentTypes.length; i++) {
            paramClassNames[i] = argumentTypes[i].getClassName();
        }
        return klass.getDeclaredMethod(methodName, paramClassNames);
    }
    
    public static String getClassNameFromBytecode(byte[] bytecode) {
        BytecodeExtractor visitor = new BytecodeExtractor();
        new ClassReader(bytecode).accept(visitor, 0);
        return visitor.className;
    }
    
    private static class BytecodeExtractor extends ClassVisitor {
        
        public BytecodeExtractor() {
            super(Opcodes.ASM4);
        }

        private String className = null;
        
        @Override
        public void visit(int version, int access, String name,
                String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            
            className = name.replace('/', '.');
        }
    }

    public static Type makeArrayType(int dims, Type elementType) {
        if (dims == 0) {
            return elementType;
        }
        StringBuilder builder = new StringBuilder();
        while (dims-- > 0) {
            builder.append('[');
        }
        builder.append(elementType.getDescriptor());
        return Type.getObjectType(builder.toString());
    }
    
    public static Class<?> getBoxingType(Type type) {
        switch (type.getSort()) {
        case Type.BOOLEAN: return Boolean.class;
        case Type.BYTE: return Byte.class;
        case Type.CHAR: return Character.class;
        case Type.SHORT: return Short.class;
        case Type.INT: return Integer.class;
        case Type.LONG: return Long.class;
        case Type.FLOAT: return Float.class;
        case Type.DOUBLE: return Double.class;
        case Type.VOID: return Void.class;
        default: return null;
        }
    }
    
    public static AnnotationMirror getAnnotation(List<AnnotationMirror> annotations, ClassMirror annotationClass) {
        for (AnnotationMirror a : annotations) {
            if (a.getClassMirror().equals(annotationClass)) {
                return a;
            }
        }
        return null;
    }
    
    public static byte[] readFromStreamMirror(ThreadMirror thread, InstanceMirror stream) {
        return NativeClassMirror.readFully(new InputStreamMirror(thread, stream));
    }
    
    public static void printAllThreads(VirtualMachineMirror vm) {
        for (ThreadMirror thread : vm.getThreads()) {
            printThreadState(thread);
        }
    }
    
    public static void printThreadState(ThreadMirror thread) {
        System.out.println(thread);
        InstanceMirror contendedMonitor = thread.getContendedMonitor();
        if (contendedMonitor != null) {
            System.out.println("\t waiting on " + contendedMonitor);
        }
        for (InstanceMirror monitor : thread.getOwnedMonitors()) {
            System.out.println("\t owns " + monitor);
        }
        for (FrameMirror frame : thread.getStackTrace()) {
            System.out.println("\t at " + frameToString(frame));
        }
    }
    
    public static String frameToString(FrameMirror frame) {
        String fileName = frame.fileName();
        int lineNumber = frame.lineNumber();
        return frame.declaringClass().getClassName() + "." + frame.methodName() +
                 (fileName != null && lineNumber >= 0 ?
                  "(" + fileName + ":" + lineNumber + ")" :
                  (fileName != null ?  "("+fileName+")" : "(Unknown Source)"));
    }
}
