package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.mirages.Mirage;

public class ClassStubs {

    public static boolean isInterface(Class<?> classLoaderLiteral, Mirage mirage) {
        return ((ClassMirror)mirage.getMirror()).isInterface();
    }
    
    public static boolean isPrimitive(Class<?> classLoaderLiteral, Mirage mirage) {
        return ((ClassMirror)mirage.getMirror()).isPrimitive();
    }
    
}
