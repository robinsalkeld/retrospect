package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.Reflection;

// This class exists in the Jikes RVM
public class VMSystemStubs extends NativeStubs {

    public VMSystemStubs(ClassHolograph klass) {
	super(klass);
    }

    public static void arraycopy(Mirage src, int srcPos, Mirage dest, int destPos, int length) {
        Reflection.arraycopy(src.getMirror(), srcPos, dest.getMirror(), destPos, length);
    }
}
