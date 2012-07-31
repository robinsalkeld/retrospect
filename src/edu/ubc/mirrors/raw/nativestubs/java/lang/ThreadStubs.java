package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.ObjectMirage;

public class ThreadStubs {
    
    public static Mirage currentThread(Class<?> classLoaderLiteral) {
        ThreadMirror mirror = ClassHolograph.currentThreadMirror.get();
        return (Mirage)ObjectMirage.make(mirror);
    }
    
    public static boolean isAlive(Class<?> classLoaderLiteral, Mirage thread) {
        return ((ThreadMirror)thread.getMirror()).getStackTrace() != null;
    }
    
    public static Mirage getStackTrace(Class<?> classLoaderLiteral, Mirage thread) {
        ObjectArrayMirror mirror = ((ThreadMirror)thread.getMirror()).getStackTrace();
        return (Mirage)ObjectMirage.make(mirror);
    }
    
    public static boolean isInterrupted(Class<?> classLoaderLiteral, Mirage thread, boolean ClearInterrupted) {
        // TODO-RS: How to implement this for a heap dump?
        return false;
    }
    
}
