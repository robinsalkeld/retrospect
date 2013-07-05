package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.NativeStubs;

public class StringStubs extends NativeStubs {

    public StringStubs(ClassHolograph klass) {
	super(klass);
    }

    public InstanceMirror intern(InstanceMirror s) {
        return klass.getVM().internString(s);
    }
}
