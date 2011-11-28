package edu.ubc.mirrors;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class ObjectMirage<T> {

    protected final ObjectMirror<T> mirror;
    
    private ObjectMirage(ObjectMirror<T> mirror) {
        this.mirror = mirror;
        
    }
    
    public ObjectMirror<T> getMirror() {
        return mirror;
    }
    
    public static <T> T make(ObjectMirror<T> mirror) {
        final Class<?> mirageClass = getMirageClass(mirror.getClassMirror());
        if (mirageClass == null) {
            throw new InternalError("No mirage class registered for class: " + mirror.getClassMirror());
        }
        
        try {
            Constructor<?> c = mirageClass.getConstructor(ObjectMirror.class);
            return (T)c.newInstance(mirror);
        } catch (NoSuchMethodException e) {
            InternalError error = new InternalError("Mirage class no-argument constructor not accessible: " + mirageClass);
            error.initCause(e);
            throw error;
        } catch (SecurityException e) {
            InternalError error = new InternalError("Mirage class constructor not accessible: " + mirageClass);
            error.initCause(e);
            throw error;
        } catch (IllegalAccessException e) {
            InternalError error = new InternalError("Mirage class constructor not accessible: " + mirageClass);
            error.initCause(e);
            throw error;
        } catch (InstantiationException e) {
            InternalError error = new InternalError("Result of ObjectMirage#getMirrorClass() is abstract: " + mirageClass);
            error.initCause(e);
            throw error;
        } catch (InvocationTargetException e) {
            InternalError error = new InternalError("Error on instantiating Mirage class: " + mirageClass);
            error.initCause(e);
            throw error;
        }
    }
    
    private static Map<ClassLoader, MirageClassLoader> mirageClassLoaders = new HashMap<ClassLoader, MirageClassLoader>();
    private static Map<ClassKey, Class<?>> mirageClasses = new HashMap<ClassKey, Class<?>>();
    
    private static class ClassKey {
        private final ClassLoader loader;
        private final String className;
        
        public ClassKey(ClassLoader loader, String className) {
            this.loader = loader;
            this.className = className;
        }
        
        public ClassKey(Class<?> c) {
            this.loader = c.getClassLoader();
            this.className = c.getName();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ClassKey)) {
                return false;
            }
            
            ClassKey other = (ClassKey)obj;
            return loader.equals(other.loader) && className.equals(other.className);
        }
        
        @Override
        public int hashCode() {
            return loader.hashCode() * 17 + className.hashCode();
        }
    }
    
    private static Class<?> getMirageClass(Class<?> original) {
        Class<?> mirageClass = mirageClasses.get(new ClassKey(original));
        if (mirageClass != null) {
            return mirageClass;
        } else {
            // If the class was loaded before the instrumentation agent was set up,
            // try accessing the .class file as a resource from the system class loader
            // (which is exactly what the ClassReader(String) constructor does).
            ClassReader readerFromClassFile;
            try {
                readerFromClassFile = new ClassReader(original.getName());
            } catch (IOException e) {
                throw new IllegalStateException("Class not trapped by agent and .class file not accessible: " + original.getName(), e);
            }
            
            return defineMirageClass(original.getName(), 
                                     original.getClassLoader(), 
                                     readerFromClassFile);
        }
    }
    
    public static Class<?> defineMirageClass(String className, ClassLoader originalLoader, ClassReader originalReader) {
        byte[] mirageClassBytes = MirageClassGenerator.generate(originalReader);
        
        MirageClassLoader mirageLoader = mirageClassLoaders.get(originalLoader);
        if (mirageLoader == null) {
            mirageLoader = new MirageClassLoader();
            mirageClassLoaders.put(originalLoader, mirageLoader);
        }
        
        Class<?> mirage = mirageLoader.defineClass(className, mirageClassBytes);
        mirageClasses.put(new ClassKey(originalLoader, className), mirage);
        return mirage;
    }
    
    private static class MirageClassLoader extends ClassLoader {
        
        private Class<?> defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }
    
    public static class Agent implements ClassFileTransformer {

        public static void premain(String options, Instrumentation instr) {
            instr.addTransformer(new Agent());
        }

        @Override
        public byte[] transform(ClassLoader loader, String className,
                Class<?> classBeingRedefined,
                ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws IllegalClassFormatException {
            
            defineMirageClass(className, loader, new ClassReader(classfileBuffer));
            return null;
        }
    }

}
