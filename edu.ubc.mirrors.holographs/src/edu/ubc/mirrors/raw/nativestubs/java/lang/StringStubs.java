package edu.ubc.mirrors.raw.nativestubs.java.lang;

import java.util.HashMap;
import java.util.Map;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.mirages.Reflection;

public class StringStubs extends NativeStubs {

    public StringStubs(ClassHolograph klass) {
	super(klass);
    }

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
    
    public InstanceMirror intern(InstanceMirror s) {
        return internMirror(s);
    }
    
}
