package edu.ubc.mirrors.raw.nativestubs.sun.reflect;

import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassGenerator;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;

public class ReflectionStubs {

    public static Mirage getCallerClass(Class<?> classLoaderLiteral, int depth) {
        // Filter out any non-holographic frames
        int nativeDepth = 1;
        Class<?> klass = sun.reflect.Reflection.getCallerClass(nativeDepth);
        while (klass != null) {
            ClassLoader loader = klass.getClassLoader();
            if (loader instanceof MirageClassLoader) {
                if (depth == 0) {
                    MirageClassLoader mirageClassLoader = (MirageClassLoader)klass.getClassLoader();
                    String className = MirageClassGenerator.getOriginalBinaryClassName(klass.getName());
                    return mirageClassLoader.makeMirage(mirageClassLoader.loadOriginalClassMirror(className));
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
        return ObjectMirage.make(frame.declaringClass());
    }
    
    public static int getClassAccessFlags(Class<?> classLoaderLiteral, Mirage klass) {
        return ((ClassMirror)klass.getMirror()).getModifiers();
    }
}
