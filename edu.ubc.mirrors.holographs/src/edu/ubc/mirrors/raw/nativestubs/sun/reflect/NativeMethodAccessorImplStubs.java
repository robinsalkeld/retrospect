package edu.ubc.mirrors.raw.nativestubs.sun.reflect;

import java.lang.reflect.InvocationTargetException;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;

public class NativeMethodAccessorImplStubs extends NativeStubs {

    public NativeMethodAccessorImplStubs(ClassHolograph klass) {
	super(klass);
    }

    public Mirage invoke0(Mirage method, Mirage target, Mirage arguments) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        InstanceMirror m = (InstanceMirror)method.getMirror();
        
        MethodMirror methodMirror = Reflection.methodMirrorForMethodInstance(m);
        
        ObjectArrayMirror argsMirror = arguments == null ? null : (ObjectArrayMirror)arguments.getMirror();
        Object[] argsArray = new Object[argsMirror == null ? 0 : argsMirror.length()];
        if (argsMirror != null) {
            for (int i = 0; i < argsArray.length; i++) {
                argsArray[i] = argsMirror.get(i);
            }
        }
        Object result = methodMirror.invoke(ThreadHolograph.currentThreadMirror(), ObjectMirage.getMirror(target), argsArray);
        return ObjectMirage.make((ObjectMirror)result);
    }
}
