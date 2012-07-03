package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.mirages.ByteArrayMirage;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;

public class ClassLoaderStubs {
    public static Mirage findLoadedClass0(Class<?> classLoaderLiteral, Mirage classLoader, Mirage name) {
        String realName = ObjectMirage.getRealStringForMirage((ObjectMirage)name);
        ClassMirror klass = ((ClassMirrorLoader)classLoader.getMirror()).findLoadedClassMirror(realName);
        return (Mirage)ObjectMirage.make(klass);
    }
    
    public static Mirage findBootstrapClass(Class<?> classLoaderLiteral, Mirage classLoader, Mirage name) {
        String realName = ObjectMirage.getRealStringForMirage((ObjectMirage)name);
        MirageClassLoader loader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        ClassMirror klass = loader.getVM().findBootstrapClassMirror(realName);
        return (Mirage)ObjectMirage.make(klass);
    }
    
    // JDK 6 version
    public static Mirage defineClass1(Class<?> classLoaderLiteral, Mirage classLoader, Mirage name, Mirage b, int off, int len,
            Mirage pd, Mirage source, boolean verify) {
        return defineClass1(classLoaderLiteral, classLoader, name, b, off, len, pd, source);
    }
    
    // JDK 7 version
    public static Mirage defineClass1(Class<?> classLoaderLiteral, Mirage classLoader, Mirage name, Mirage b, int off, int len,
            Mirage pd, Mirage source) {
        ClassMirrorLoader classLoaderMirror = (ClassMirrorLoader)classLoader.getMirror();
        String realName = ObjectMirage.getRealStringForMirage((ObjectMirage)name);
        InstanceMirror pdMirror = ((InstanceMirror)Reflection.getMirror(pd));
        InstanceMirror sourceMirror = ((InstanceMirror)Reflection.getMirror(source));
        
        ClassMirror newClass = classLoaderMirror.defineClass1(realName, (ByteArrayMirror)b, off, len, pdMirror, sourceMirror);
        return (Mirage)ObjectMirage.make(newClass);
    }
}
