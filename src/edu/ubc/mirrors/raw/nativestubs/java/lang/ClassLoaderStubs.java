package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.ObjectMirage;

public class ClassLoaderStubs {
    public static Mirage findLoadedClass0(Class<?> classLoaderLiteral, Mirage classLoader, Mirage name) {
        String realName = ObjectMirage.getRealStringForMirage((ObjectMirage)name);
        ClassMirror klass = ((ClassMirrorLoader)classLoader.getMirror()).findLoadedClassMirror(realName);
        return (Mirage)ObjectMirage.make(klass, classLoaderLiteral);
    }
    
    public static Mirage findBootstrapClass(Class<?> classLoaderLiteral, Mirage classLoader, Mirage name) {
        String realName = ObjectMirage.getRealStringForMirage((ObjectMirage)name);
        MirageClassLoader loader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        ClassMirror klass = loader.getVM().findBootstrapClassMirror(realName);
        return (Mirage)ObjectMirage.make(klass, classLoaderLiteral);
    }
    
}
