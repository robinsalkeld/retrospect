package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.NativeStubs;

// This class exists in the Jikes RVM
public class VMSystemStubs extends NativeStubs {

    public VMSystemStubs(ClassHolograph klass) {
	super(klass);
    }

    public static void arraycopy(InstanceMirror src, int srcPos, InstanceMirror dest, int destPos, int length) {
        Reflection.arraycopy(src, srcPos, dest, destPos, length);
    }
}
