package edu.ubc.mirrors.raw.nativestubs.java.lang;

import java.util.HashMap;
import java.util.Map;

import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mutable.MutableClassMirrorLoader;
import edu.ubc.mirrors.mutable.MutableVirtualMachineMirror;
import edu.ubc.mirrors.raw.NativeObjectMirror;

public class ThreadStubs {
    
    private static Map<Thread, Mirage> threadMirages = new HashMap<Thread, Mirage>();
    
    public static Mirage currentThread(Class<?> classLoaderLiteral) {
        Thread current = Thread.currentThread();
        Mirage mirage = threadMirages.get(current);
        if (mirage != null) {
            return mirage;
        }
        
        // TODO-RS: Encapsulate the mutable layer better!
        MirageClassLoader loader = ((MirageClassLoader)classLoaderLiteral.getClassLoader());
        ObjectMirror mirror = ((MutableVirtualMachineMirror)loader.getVM()).getWrappedMirror(NativeObjectMirror.makeMirror(current));
        mirage = (Mirage)ObjectMirage.make(mirror, classLoaderLiteral);
        threadMirages.put(current, mirage);
        return mirage;
    }
    
    public static boolean isAlive(Class<?> classLoaderLiteral, Mirage thread) {
        return ((ThreadMirror)thread.getMirror()).getStackTrace() != null;
    }
    
    public static Mirage getStackTrace(Class<?> classLoaderLiteral, Mirage thread) {
        ObjectArrayMirror mirror = ((ThreadMirror)thread.getMirror()).getStackTrace();
        return (Mirage)ObjectMirage.make(mirror, classLoaderLiteral);
    }
    
}
