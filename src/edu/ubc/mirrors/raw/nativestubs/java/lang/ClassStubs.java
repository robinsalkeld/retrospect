package edu.ubc.mirrors.raw.nativestubs.java.lang;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.raw.ArrayClassMirror;

public class ClassStubs {

    public static boolean isInterface(Class<?> classLoaderLiteral, Mirage mirage) {
        return ((ClassMirror)mirage.getMirror()).isInterface();
    }
    
    public static boolean isPrimitive(Class<?> classLoaderLiteral, Mirage mirage) {
        return ((ClassMirror)mirage.getMirror()).isPrimitive();
    }
    
    public static boolean isArray(Class<?> classLoaderLiteral, Mirage mirage) {
        return ((ClassMirror)mirage.getMirror()).isArray();
    }
    
    public static ClassMirror classMirrorForName(String name, boolean resolve, ClassMirrorLoader loader) throws ClassNotFoundException {
        Type type = Type.getObjectType(name);
        if (type.getSort() == Type.ARRAY) {
            return new ArrayClassMirror(loader, type);
        } else {
            return MirageClassLoader.loadClassMirror(loader, name);
        }
    }
}
