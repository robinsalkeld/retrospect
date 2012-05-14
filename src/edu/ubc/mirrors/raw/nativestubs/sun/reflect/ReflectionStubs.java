package edu.ubc.mirrors.raw.nativestubs.sun.reflect;

import sun.reflect.Reflection;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassGenerator;
import edu.ubc.mirrors.mirages.MirageClassLoader;

public class ReflectionStubs {

    public static Mirage getCallerClass(Class<?> classLoaderLiteral, int depth) {
        Class<?> klass = Reflection.getCallerClass(depth + 1);
        // Need to map from the native Class<?> to a mirage on the ClassMirror
        MirageClassLoader loader = (MirageClassLoader)klass.getClassLoader();
        String className = MirageClassGenerator.getOriginalBinaryClassName(klass.getName());
        return (Mirage)loader.makeMirage(loader.loadOriginalClassMirror(className));
    }
    
    public static int getClassAccessFlags(Class<?> classLoaderLiteral, Mirage klass) {
        return ((ClassMirror)klass.getMirror()).getModifiers();
    }
}
