package edu.ubc.mirrors.raw.nativestubs.sun.reflect;

import java.lang.reflect.InvocationTargetException;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.ObjectMirage;

public class NativeConstructorAccessorImplStubs {

    public static Mirage newInstance0(Class<?> classLoaderLiteral, Mirage constructor, Mirage arguments) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        InstanceMirror cons = (InstanceMirror)constructor.getMirror();
        
        ClassMirror declaringClass = (ClassMirror)HolographInternalUtils.getField(cons, "clazz");
        ObjectArrayMirror parameterTypesMirror = (ObjectArrayMirror)HolographInternalUtils.getField(cons, "parameterTypes");
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
        
        ObjectArrayMirror argsMirror = arguments == null ? null : (ObjectArrayMirror)arguments.getMirror();
        Object[] argsArray = new Object[argsMirror == null ? 0 : argsMirror.length()];
        if (argsMirror != null) {
            for (int i = 0; i < argsArray.length; i++) {
                argsArray[i] = argsMirror.get(i);
            }
        }
        return ObjectMirage.make(consMirror.newInstance(ThreadHolograph.currentThreadMirror(), argsArray));
    }
    
}
