package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class StringStubs extends NativeStubs {

    public StringStubs(ClassHolograph klass) {
	super(klass);
    }

    @StubMethod
    public InstanceMirror intern(InstanceMirror s) {
        return klass.getVM().internString(s);
    }
}
