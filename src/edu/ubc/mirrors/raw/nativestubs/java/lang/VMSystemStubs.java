package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.mirages.Mirage;

public class VMSystemStubs {

    public static void arraycopy(Class<?> classLoaderLiteral, Mirage src, int srcPos, Mirage dest, int destPos, int length) {
        SystemStubs.arraycopy(classLoaderLiteral, src, srcPos, dest, destPos, length);
    }
}
