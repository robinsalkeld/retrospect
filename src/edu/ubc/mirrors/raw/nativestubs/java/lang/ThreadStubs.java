package edu.ubc.mirrors.raw.nativestubs.java.lang;

import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.fieldmap.FieldMapStackTraceElementMirror;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.ObjectMirage;

public class ThreadStubs {
    
    public static Mirage currentThread(Class<?> classLoaderLiteral) {
        ThreadMirror mirror = ThreadHolograph.currentThreadMirror();
        return (Mirage)ObjectMirage.make(mirror);
    }
    
    public static boolean isAlive(Class<?> classLoaderLiteral, Mirage thread) {
        return ((ThreadMirror)thread.getMirror()).getStackTrace() != null;
    }
    
    public static Mirage getStackTrace(Class<?> classLoaderLiteral, Mirage thread) {
	MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineMirror vm = callingLoader.getVM();
        
        List<FrameMirror> trace = ((ThreadMirror)thread.getMirror()).getStackTrace();
        int size = trace.size();
        ClassMirror stackTraceElementClass = vm.findBootstrapClassMirror(StackTraceElement.class.getName());
        ObjectArrayMirror result = (ObjectArrayMirror)stackTraceElementClass.newArray(size);
        for (int i = 0; i < size; i++) {
	    result.set(i, new FieldMapStackTraceElementMirror(vm, trace.get(i)));
	}
        return (Mirage)ObjectMirage.make(result);
    }
    
    public static boolean isInterrupted(Class<?> classLoaderLiteral, Mirage thread, boolean ClearInterrupted) {
        // TODO-RS: How to implement this for a heap dump?
        return false;
    }
    
}
