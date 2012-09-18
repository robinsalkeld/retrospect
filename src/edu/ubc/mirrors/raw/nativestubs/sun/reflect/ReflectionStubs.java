package edu.ubc.mirrors.raw.nativestubs.sun.reflect;

import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
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
        }
        
        // Off the top of the holographic stack, so refer to the original stack.
        // Unfortunately here we only have class names rather than actual classes
        // so if any are ambiguous we're hooped.
        ThreadMirror currentThread = ThreadHolograph.currentThreadMirror();
        ObjectArrayMirror stack = currentThread.getStackTrace();
        int frameIndex = stack.length() - 1 - depth;
        if (frameIndex < 0) {
            return null;
        }
        InstanceMirror frame = (InstanceMirror)stack.get(frameIndex);
        String className = Reflection.getRealStringForMirror((InstanceMirror)Reflection.getField(frame, "declaringClass"));
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineMirror vm = callingLoader.getVM();
        List<ClassMirror> matchingClasses = vm.findAllClasses(className, false);
        if (matchingClasses.isEmpty()) {
            // This indicates an error in the underlying VM
            throw new InternalError();
        } else if (matchingClasses.size() > 1) {
            // This is just unfortunate but could happen
            throw new InternalError("Ambiguous class name on stack: " + className);
        }
        return ObjectMirage.make(matchingClasses.get(0));
    }
    
    public static int getClassAccessFlags(Class<?> classLoaderLiteral, Mirage klass) {
        return ((ClassMirror)klass.getMirror()).getModifiers();
    }
}
