package edu.ubc.mirrors.raw.nativestubs.sun.reflect;

import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.holograms.HologramClassGenerator;
import edu.ubc.mirrors.holograms.HologramClassLoader;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class ReflectionStubs extends NativeStubs {

    public ReflectionStubs(ClassHolograph klass) {
	super(klass);
    }

    @StubMethod
    public static ClassMirror getCallerClassMirror(int depth) {
        // Filter out any non-holographic frames
        int nativeDepth = 1;
        Class<?> klass = sun.reflect.Reflection.getCallerClass(nativeDepth);
        while (klass != null) {
            ClassLoader loader = klass.getClassLoader();
            if (loader instanceof HologramClassLoader) {
                if (depth == 0) {
                    HologramClassLoader hologramClassLoader = (HologramClassLoader)klass.getClassLoader();
                    String className = HologramClassGenerator.getOriginalBinaryClassName(klass.getName());
                    return hologramClassLoader.loadOriginalClassMirror(className);
                }
                depth--;
            }
            nativeDepth++;
            klass = sun.reflect.Reflection.getCallerClass(nativeDepth);
        }
        
        // Off the top of the holographic stack, so refer to the original stack.
        ThreadMirror currentThread = ThreadHolograph.currentThreadMirror();
        List<FrameMirror> stack = currentThread.getStackTrace();
        int frameIndex = stack.size() - 1 - depth;
        if (frameIndex < 0) {
            return null;
        }
        FrameMirror frame = stack.get(frameIndex);
        return frame.declaringClass();
    }
    
    @StubMethod
    public ClassMirror getCallerClass(int depth) {
        return getCallerClassMirror(depth);
    }
    
    @StubMethod
    public ClassMirror getCallerClass() {
        return getCallerClassMirror(1);
    }
    
    @StubMethod
    public int getClassAccessFlags(ClassMirror klass) {
        return klass.getModifiers();
    }
}
