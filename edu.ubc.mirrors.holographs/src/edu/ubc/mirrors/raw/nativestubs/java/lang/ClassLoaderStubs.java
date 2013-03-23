package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.nativestubs.sun.reflect.ReflectionStubs;

public class ClassLoaderStubs extends NativeStubs {
    
    public ClassLoaderStubs(ClassHolograph klass) {
	super(klass);
    }

    public Mirage findLoadedClass0(Mirage classLoader, Mirage name) {
        String realName = ObjectMirage.getRealStringForMirage((ObjectMirage)name);
        ClassMirror klass = ((ClassMirrorLoader)classLoader.getMirror()).findLoadedClassMirror(realName);
        return (Mirage)ObjectMirage.make(klass);
    }
    
    public Mirage findBootstrapClass(Mirage classLoader, Mirage name) {
        String realName = ObjectMirage.getRealStringForMirage((ObjectMirage)name);
        ClassMirror result = klass.getVM().findBootstrapClassMirror(realName);
        return (Mirage)ObjectMirage.make(result);
    }
    
    // JDK 6 version
    public Mirage defineClass1(Mirage classLoader, Mirage name, Mirage b, int off, int len,
            Mirage pd, Mirage source, boolean verify) {
        return defineClass1(classLoader, name, b, off, len, pd, source);
    }
    
    public static Mirage defineClass(Mirage classLoader, Mirage name, Mirage b, int off, int len,
            Mirage pd, Mirage source) {
	
	ClassMirrorLoader classLoaderMirror = (ClassMirrorLoader)classLoader.getMirror();
        String realName = ObjectMirage.getRealStringForMirage((ObjectMirage)name);
        InstanceMirror pdMirror = ((InstanceMirror)Reflection.getMirror(pd));
        InstanceMirror sourceMirror = ((InstanceMirror)Reflection.getMirror(source));
        
        ClassMirror newClass = classLoaderMirror.defineClass1(realName, (ByteArrayMirror)b, off, len, pdMirror, sourceMirror);
        return (Mirage)ObjectMirage.make(newClass);
    }
    
    // JDK 7 version
    public Mirage defineClass1(Mirage classLoader, Mirage name, Mirage b, int off, int len,
            Mirage pd, Mirage source) {
        
        return defineClass(classLoader, name, b, off, len, pd, source);
    }
    
    // Note: the specification of this method seems broken: it implies the result should
    // always extend ClassLoader but I don't see how that's true.
    public Mirage getCaller(int depth) {
        ClassMirror callerClass = ReflectionStubs.getCallerClassMirror(depth);
        return ObjectMirage.make(callerClass);
    }
}
