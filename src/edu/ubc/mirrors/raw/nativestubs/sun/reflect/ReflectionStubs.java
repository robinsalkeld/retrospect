package edu.ubc.mirrors.raw.nativestubs.sun.reflect;

import sun.reflect.Reflection;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassGenerator;
import edu.ubc.mirrors.mirages.MirageClassLoader;

public class ReflectionStubs {

    public static Mirage getCallerClass(Class<?> classLoaderLiteral, int depth) {
        Class<?> klass = Reflection.getCallerClass(depth + 1);
        // Need to map from the native Class<?> to a mirage on the ClassMirror
        MirageClassLoader loader = (MirageClassLoader)klass.getClassLoader();
        String className = MirageClassGenerator.getOriginalBinaryClassName(klass.getName());
        try {
            return (Mirage)loader.makeMirage(loader.getOriginalClassMirrorLoader().loadClassMirror(className));
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getMessage());
        }
    }
    
}
