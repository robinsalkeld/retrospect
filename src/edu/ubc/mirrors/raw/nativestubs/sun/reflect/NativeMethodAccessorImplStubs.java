package edu.ubc.mirrors.raw.nativestubs.sun.reflect;

import java.lang.reflect.InvocationTargetException;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;

public class NativeMethodAccessorImplStubs {

    public static Mirage invoke0(Class<?> classLoaderLiteral, Mirage method, Mirage target, Mirage arguments) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        InstanceMirror m = (InstanceMirror)method.getMirror();
        
        ClassMirror declaringClass = (ClassMirror)Reflection.getField(m, "clazz");
        String name = Reflection.getRealStringForMirror((InstanceMirror)Reflection.getField(m, "name"));
        ObjectArrayMirror parameterTypesMirror = (ObjectArrayMirror)Reflection.getField(m, "parameterTypes");
        ClassMirror[] parameterTypes = new ClassMirror[parameterTypesMirror.length()];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i++] = (ClassMirror)parameterTypesMirror.get(i);
        }

        MethodMirror methodMirror;
        try {
            methodMirror = declaringClass.getMethod(name, parameterTypes);
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
        Object result = methodMirror.invoke(ThreadHolograph.currentThreadMirror(), ObjectMirage.getMirror(target), argsArray);
        return ObjectMirage.make((ObjectMirror)result);
    }
    
}
