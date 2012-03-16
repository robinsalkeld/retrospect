package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.ObjectMirror;

public class VMSystemStubs {

    public static void arraycopy(ObjectMirror src, int srcPos, ObjectMirror dest, int destPos, int length) {
        System.arraycopy(src, srcPos, dest, destPos, length);
    }
}
