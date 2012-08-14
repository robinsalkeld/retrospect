package edu.ubc.mirrors.raw.nativestubs.java.lang;

import java.util.HashMap;
import java.util.Map;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;

public class StringStubs {

    private static Map<String, InstanceMirror> internedStrings =
           new HashMap<String, InstanceMirror>();
    
    public static InstanceMirror internMirror(InstanceMirror s) {
        String realString = Reflection.getRealStringForMirror(s);
        InstanceMirror interned = internedStrings.get(realString);
        if (interned == null) {
            interned = s;
            internedStrings.put(realString, s);
        }
        return interned;
    }
    
    public static Mirage intern(Class<?> classLoaderLiteral, Mirage s) {
        InstanceMirror mirror = (InstanceMirror)s.getMirror();
        InstanceMirror interned = internMirror(mirror);
        return ObjectMirage.make(interned);
    }
    
}
