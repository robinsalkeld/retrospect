package edu.ubc.mirrors.raw.nativestubs.java.lang;

import java.util.HashMap;
import java.util.Map;

import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.mutable.MutableClassMirrorLoader;
import edu.ubc.mirrors.mutable.MutableInstanceMirror;
import edu.ubc.mirrors.raw.NativeObjectMirror;

public class ThreadStubs {
    
    private static Map<Thread, ObjectMirror> threadMirrors = new HashMap<Thread, ObjectMirror>();
    
    public static ObjectMirror currentThread() {
        Thread current = Thread.currentThread();
        ObjectMirror mirror = threadMirrors.get(current);
        if (mirror != null) {
            return mirror;
        }
        
        mirror = MutableClassMirrorLoader.makeMirrorStatic(new NativeObjectMirror(current));
        threadMirrors.put(current, mirror);
        return mirror;
    }
    
}
