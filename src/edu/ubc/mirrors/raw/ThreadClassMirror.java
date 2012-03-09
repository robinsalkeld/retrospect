package edu.ubc.mirrors.raw;

import java.util.HashMap;
import java.util.Map;

import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.mutable.MutableClassMirrorLoader;
import edu.ubc.mirrors.mutable.MutableInstanceMirror;

public class ThreadClassMirror extends NativeClassMirror {

    public ThreadClassMirror(Class<?> klass) {
        super(klass);
    }
    
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
