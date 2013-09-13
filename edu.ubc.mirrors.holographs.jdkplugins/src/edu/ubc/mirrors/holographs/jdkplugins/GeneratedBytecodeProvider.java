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
package edu.ubc.mirrors.holographs.jdkplugins;

import java.lang.ref.Reference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodHandle;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ClassMirrorBytecodeProvider;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;

public class GeneratedBytecodeProvider implements ClassMirrorBytecodeProvider {

    private static final Map<ClassMirror, List<String>> proxyClassInterfaces =
                 new HashMap<ClassMirror, List<String>>();
    
    private List<String> getInterfacesForProxyClass(ClassMirror klass) throws IllegalAccessException {
        List<String> result = proxyClassInterfaces.get(klass);
        if (result != null) {
            return result;
        }
        
        ClassMirror proxyClass = klass.getVM().findBootstrapClassMirror(Proxy.class.getName());
        
        // Unfortunately we can't just call ClassMirror.getInterfaceMirrors(), since that
        // might depend on the very bytecode we're trying to find!
        // The list of interfaces is also not stored in any other way on the proxy class.
        // Luckily Proxy has a cache that uses the interface names as keys.
        InstanceMirror /* Map */ loaderToCache = (InstanceMirror)proxyClass.getStaticFieldValues().get(proxyClass.getDeclaredField("loaderToCache"));
        for (Map.Entry<ObjectMirror, ObjectMirror> loaderEntry : Reflection.mapEntries(loaderToCache).entrySet()) {
            InstanceMirror cache = (InstanceMirror)loaderEntry.getValue();
            for (Map.Entry<ObjectMirror, ObjectMirror> entry : Reflection.mapEntries(cache).entrySet()) {
                List<ObjectMirror> interfaceNameMirrors = Reflection.collectionValues(entry.getKey());
                List<String> interfaceNames = new ArrayList<String>();
                for (ObjectMirror m : interfaceNameMirrors) {
                    interfaceNames.add(Reflection.getRealStringForMirror((InstanceMirror)m));
                }
                InstanceMirror ref = (InstanceMirror)entry.getValue();
                ClassMirror cachedClass = (ClassMirror)Reflection.invokeMethodHandle(ref,
                        new MethodHandle() {
                            protected void methodCall() throws Throwable {
                                ((Reference<?>)null).get();
                            }
                        });
                proxyClassInterfaces.put(cachedClass, interfaceNames);
            }
        }
            
        return proxyClassInterfaces.get(klass);
    }
    
    public byte[] getProxyBytecode(ClassMirror classMirror) {
        VirtualMachineMirror vm = classMirror.getVM();
        try {
            // Just reuse the same class that would have generated the bytecode
            ClassMirror classClass = vm.findBootstrapClassMirror(Class.class.getName());
            List<String> interfaceNames = getInterfacesForProxyClass(classMirror);
            ObjectArrayMirror interfacesArrayMirror = (ObjectArrayMirror)classClass.newArray(interfaceNames.size());
            int index = 0;
            for (String interfaceName : interfaceNames) {
                interfacesArrayMirror.set(index++, Reflection.classMirrorForName(vm, 
                        ThreadHolograph.currentThreadMirror(), interfaceName, false, classMirror.getLoader()));
            }
            
            // Could cache this per VM, but likely not worth it.
            ClassMirror generator = vm.findBootstrapClassMirror("sun.misc.ProxyGenerator");
            MethodMirror generateMethod = generator.getDeclaredMethod("generateProxyClass", 
                        vm.findBootstrapClassMirror(String.class.getName()),
                        vm.getArrayClass(1, classClass));
            
            ByteArrayMirror bytesArray;
            bytesArray = (ByteArrayMirror)generateMethod.invoke(ThreadHolograph.currentThreadMirror(), null,
                    Reflection.makeString(vm, classMirror.getClassName()), interfacesArrayMirror);
            byte[] result = new byte[bytesArray.length()];
            ByteArrayMirror nativeBytes = new NativeByteArrayMirror(result);
            Reflection.arraycopy(bytesArray, 0, nativeBytes, 0, result.length);
            return result;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (MirrorInvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    public byte[] getGeneratedConstructorAccessorBytecode(ClassMirror classMirror) {
        VirtualMachineMirror vm = classMirror.getVM();
        try {
            ClassMirror consClass = vm.findBootstrapClassMirror(Constructor.class.getName());
            FieldMirror accessorField = consClass.getDeclaredField("constructorAccessor");
            ClassMirror delegatingClass = vm.findBootstrapClassMirror("sun.reflect.DelegatingConstructorAccessorImpl");
            FieldMirror delegateField = delegatingClass.getDeclaredField("delegate");
            for (ObjectMirror method : consClass.getInstances()) {
                InstanceMirror m = (InstanceMirror)method;
                ObjectMirror accessor = m.get(accessorField);
                if (accessor != null) {
                    while (accessor.getClassMirror().equals(delegatingClass)) {
                        accessor = ((InstanceMirror)accessor).get(delegateField);
                    }
                    
                    if (accessor.getClassMirror().equals(classMirror)) {
                        ConstructorMirror consMirror = Reflection.constructorMirrorForConstructorInstance(m);
                        
                        // Could cache this per VM, but likely not worth it.
                        ClassMirror classClass = vm.findBootstrapClassMirror(Class.class.getName());
                        ClassMirror classArrayClass = vm.getArrayClass(1, classClass);
                        ClassMirror generatorClass = vm.findBootstrapClassMirror("sun.reflect.MethodAccessorGenerator");
                        MethodMirror generateMethod = generatorClass.getDeclaredMethod("generateConstructor", 
                                    classClass,
                                    classArrayClass,
                                    classArrayClass,
                                    vm.getPrimitiveClass("int"));
                        
                        // This will have the side-effect of defining a new class, but it should
                        // be totally transparent.
                        InstanceMirror generator = generatorClass.getConstructor().newInstance(ThreadHolograph.currentThreadMirror());
                        InstanceMirror duplicateAccessor = (InstanceMirror)generateMethod.invoke(ThreadHolograph.currentThreadMirror(), generator, 
                                consMirror.getDeclaringClass(),
                                Reflection.toArray(classClass, consMirror.getParameterTypes()),
                                Reflection.toArray(classClass, consMirror.getExceptionTypes()),
                                consMirror.getModifiers());
                        ClassMirror newClass = duplicateAccessor.getClassMirror();
                        
                        // Rename from whatever was generated to what we want.
                        byte[] generatedBytecode = newClass.getBytecode();
                        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                        ClassVisitor visitor = classWriter;
                        final String internalName = classMirror.getClassName().replace('.', '/');
                        final String generatedInternalName = newClass.getClassName().replace('.', '/');
                        visitor = new RemappingClassAdapter(visitor, new Remapper() {
                            @Override
                            public String map(String typeName) {
                                if (typeName.equals(generatedInternalName)) {
                                    return internalName;
                                }
                                return super.map(typeName);
                            }
                        });
                        new ClassReader(generatedBytecode).accept(visitor, 0);
                        return classWriter.toByteArray();
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (MirrorInvocationTargetException e) {
            throw new RuntimeException(e);
        }
        
        return null;
    }
    
    public byte[] getGeneratedMethodAccessorBytecode(ClassMirror classMirror) {
        VirtualMachineMirror vm = classMirror.getVM();
        try {
            ClassMirror methodClass = vm.findBootstrapClassMirror(Method.class.getName());
            FieldMirror accessorField = methodClass.getDeclaredField("methodAccessor");
            ClassMirror delegatingClass = vm.findBootstrapClassMirror("sun.reflect.DelegatingMethodAccessorImpl");
            FieldMirror delegateField = delegatingClass.getDeclaredField("delegate");
            for (ObjectMirror method : methodClass.getInstances()) {
                InstanceMirror m = (InstanceMirror)method;
                ObjectMirror accessor = m.get(accessorField);
                if (accessor != null) {
                    while (accessor.getClassMirror().equals(delegatingClass)) {
                        accessor = ((InstanceMirror)accessor).get(delegateField);
                    }
                    
                    if (accessor.getClassMirror().equals(classMirror)) {
                        MethodMirror methodMirror = Reflection.methodMirrorForMethodInstance(m);
                        
                        // Could cache this per VM, but likely not worth it.
                        ClassMirror classClass = vm.findBootstrapClassMirror(Class.class.getName());
                        ClassMirror classArrayClass = vm.getArrayClass(1, classClass);
                        ClassMirror generatorClass = vm.findBootstrapClassMirror("sun.reflect.MethodAccessorGenerator");
                        MethodMirror generateMethod = generatorClass.getDeclaredMethod("generateMethod", 
                                    classClass,
                                    vm.findBootstrapClassMirror(String.class.getName()),
                                    classArrayClass,
                                    classClass,
                                    classArrayClass,
                                    vm.getPrimitiveClass("int"));
                        
                        // This will have the side-effect of defining a new class, but it should
                        // be totally transparent.
                        InstanceMirror generator = generatorClass.getConstructor().newInstance(ThreadHolograph.currentThreadMirror());
                        InstanceMirror duplicateAccessor = (InstanceMirror)generateMethod.invoke(ThreadHolograph.currentThreadMirror(), generator, 
                                methodMirror.getDeclaringClass(),
                                Reflection.makeString(vm, methodMirror.getName()),
                                Reflection.toArray(classClass, methodMirror.getParameterTypes()),
                                methodMirror.getReturnType(),
                                Reflection.toArray(classClass, methodMirror.getExceptionTypes()),
                                methodMirror.getModifiers());
                        ClassMirror newClass = duplicateAccessor.getClassMirror();
                        
                        // Rename from whatever was generated to what we want.
                        byte[] generatedBytecode = newClass.getBytecode();
                        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                        ClassVisitor visitor = classWriter;
                        final String internalName = classMirror.getClassName().replace('.', '/');
                        final String generatedInternalName = newClass.getClassName().replace('.', '/');
                        visitor = new RemappingClassAdapter(visitor, new Remapper() {
                            @Override
                            public String map(String typeName) {
                                if (typeName.equals(generatedInternalName)) {
                                    return internalName;
                                }
                                return super.map(typeName);
                            }
                        });
                        new ClassReader(generatedBytecode).accept(visitor, 0);
                        return classWriter.toByteArray();
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (MirrorInvocationTargetException e) {
            throw new RuntimeException(e);
        }
        
        return null;
    }
    
    @Override
    public byte[] getBytecode(ClassMirror classMirror) {
        VirtualMachineMirror vm = classMirror.getVM();
        ClassMirror proxyClass = vm.findBootstrapClassMirror(Proxy.class.getName());
        if (proxyClass.equals(classMirror.getSuperClassMirror())) {
            return getProxyBytecode(classMirror);
        } else if (classMirror.getClassName().startsWith("sun.reflect.GeneratedMethodAccessor")) {
            return getGeneratedMethodAccessorBytecode(classMirror);
        } else if (classMirror.getClassName().startsWith("sun.reflect.GeneratedConstructorAccessor")) {
            return getGeneratedConstructorAccessorBytecode(classMirror);
        }

        return null;
    }

}
