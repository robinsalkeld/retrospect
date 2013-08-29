package edu.ubc.mirrors.raw.nativestubs.java.util;

import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.holograms.HologramClassLoader;
import edu.ubc.mirrors.holograms.ObjectHologram;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class ResourceBundleStubs extends NativeStubs {

    public ResourceBundleStubs(ClassHolograph klass) {
        super(klass);
    }

    @StubMethod
    public ObjectArrayMirror getClassContext() {
        List<ClassMirror> result = new ArrayList<ClassMirror>();
        
        int nativeDepth = 1;
        Class<?> klass = sun.reflect.Reflection.getCallerClass(nativeDepth);
        while (klass != null) {
            // Filter out any non-holographic frames
            ClassLoader loader = klass.getClassLoader();
            if (loader instanceof HologramClassLoader) {
                result.add(ObjectHologram.getClassMirrorForHolographicClass(klass));
            }
            nativeDepth++;
            klass = sun.reflect.Reflection.getCallerClass(nativeDepth);
        }
        
        ThreadMirror currentThread = ThreadHolograph.currentThreadMirror();
        for (FrameMirror frame : currentThread.getStackTrace()) {
            result.add(frame.declaringClass());
        }
        
        // Remove the frame for the native method
        result.remove(0);
        
        return Reflection.toArray(getVM().findBootstrapClassMirror(Class.class.getName()), result);
    }
    
}
