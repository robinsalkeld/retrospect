package edu.ubc.mirrors.raw.nativestubs.sun.reflect;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holograms.ObjectHologram;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class NativeMethodAccessorImplStubs extends NativeStubs {

    public NativeMethodAccessorImplStubs(ClassHolograph klass) {
	super(klass);
    }

    @StubMethod
    public ObjectMirror invoke0(InstanceMirror method, InstanceMirror target, ObjectArrayMirror argsMirror) throws IllegalArgumentException, InstantiationException, IllegalAccessException, MirrorInvocationTargetException {
        MethodMirror methodMirror = Reflection.methodMirrorForMethodInstance(method);
        
        Object[] argsArray = new Object[argsMirror == null ? 0 : argsMirror.length()];
        if (argsMirror != null) {
            for (int i = 0; i < argsArray.length; i++) {
                argsArray[i] = argsMirror.get(i);
            }
        }
        Object result = methodMirror.invoke(ThreadHolograph.currentThreadMirror(), ObjectHologram.getMirror(target), argsArray);
        return (ObjectMirror)result;
    }
}