package edu.ubc.mirrors.raw.nativestubs.sun.reflect;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.holographs.ThreadHolograph;

public class NativeConstructorAccessorImplStubs extends NativeStubs {

    public NativeConstructorAccessorImplStubs(ClassHolograph klass) {
	super(klass);
    }

    public ObjectMirror newInstance0(InstanceMirror constructor, ObjectArrayMirror argsMirror) throws IllegalArgumentException, InstantiationException, IllegalAccessException, MirrorInvocationTargetException {
        
        ClassMirror declaringClass = (ClassMirror)HolographInternalUtils.getField(constructor, "clazz");
        ObjectArrayMirror parameterTypesMirror = (ObjectArrayMirror)HolographInternalUtils.getField(constructor, "parameterTypes");
        ClassMirror[] parameterTypes = new ClassMirror[parameterTypesMirror.length()];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = (ClassMirror)parameterTypesMirror.get(i);
        }

        ConstructorMirror consMirror;
        try {
            consMirror = declaringClass.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
        
        Object[] argsArray = new Object[argsMirror == null ? 0 : argsMirror.length()];
        if (argsMirror != null) {
            for (int i = 0; i < argsArray.length; i++) {
                argsArray[i] = argsMirror.get(i);
            }
        }
        return consMirror.newInstance(ThreadHolograph.currentThreadMirror(), argsArray);
    }
    
}
