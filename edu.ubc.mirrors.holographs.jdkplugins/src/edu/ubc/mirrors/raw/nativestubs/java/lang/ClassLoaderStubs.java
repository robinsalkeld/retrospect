package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;
import edu.ubc.mirrors.raw.nativestubs.sun.reflect.ReflectionStubs;

public class ClassLoaderStubs extends NativeStubs {
    
    public ClassLoaderStubs(ClassHolograph klass) {
	super(klass);
    }

    @StubMethod
    public ClassMirror findLoadedClass0(ClassMirrorLoader classLoader, InstanceMirror name) {
        String realName = Reflection.getRealStringForMirror(name);
        return classLoader.findLoadedClassMirror(realName);
    }
    
    @StubMethod
    public ClassMirror findBootstrapClass(ClassMirrorLoader classLoader, InstanceMirror name) {
        String realName = Reflection.getRealStringForMirror(name);
        return klass.getVM().findBootstrapClassMirror(realName);
    }
    
    // JDK 6 version
    @StubMethod
    public ClassMirror defineClass1(ClassMirrorLoader classLoader, InstanceMirror name, ByteArrayMirror b, int off, int len,
            InstanceMirror pd, InstanceMirror source, boolean verify) {
        return defineClass1(classLoader, name, b, off, len, pd, source);
    }
    
    public static ClassMirror defineClass(ClassMirrorLoader classLoader, InstanceMirror name, ByteArrayMirror b, int off, int len,
            InstanceMirror pd, InstanceMirror source) {
	
        String realName = Reflection.getRealStringForMirror(name);
        
        return classLoader.defineClass1(realName, b, off, len, pd, source);
    }
    
    // JDK 7 version
    @StubMethod
    public ClassMirror defineClass1(ClassMirrorLoader classLoader, InstanceMirror name, ByteArrayMirror b, int off, int len,
            InstanceMirror pd, InstanceMirror source) {
        
        return defineClass(classLoader, name, b, off, len, pd, source);
    }
    
    // Note: the generic return type Class<? extends ClassLoader> of this method seems broken: it implies the result should
    // always extend ClassLoader but I don't see how that's true. Alternatively it could mean "loader of the class at the given depth"
    // but then the method wouldn't have the correct semantics for the one place it's called.
    @StubMethod
    public ClassMirror getCaller(int depth) {
        return ReflectionStubs.getCallerClassMirror(depth);
    }
}
