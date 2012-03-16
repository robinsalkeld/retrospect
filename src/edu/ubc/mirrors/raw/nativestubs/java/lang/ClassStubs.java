package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectMirror;

public class ClassStubs {

    public static boolean isInterface(ObjectMirror mirror) {
        return ((ClassMirror)mirror).isInterface();
    }
    
    public static boolean isPrimitive(ObjectMirror mirror) {
        return ((ClassMirror)mirror).isPrimitive();
    }
    
}
