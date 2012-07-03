package edu.ubc.mirrors.raw.nativestubs.java.lang;

import java.util.HashMap;
import java.util.Map;

import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.ObjectMirage;

public class StringStubs {

    private static Map<String, Mirage> internedStrings =
           new HashMap<String, Mirage>();
    
    public static Mirage intern(Class<?> classLoaderLiteral, Mirage s) {
        String realString = ObjectMirage.getRealStringForMirage((ObjectMirage)s);
        Mirage interned = internedStrings.get(realString);
        if (interned == null) {
            interned = s;
            internedStrings.put(realString, s);
        }
        return interned;
    }
    
}
