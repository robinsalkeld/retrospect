package edu.ubc.mirrors.holographs;

import java.lang.reflect.InvocationTargetException;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.mirages.Reflection;

/**
 * Utility methods for the internal holograph implementation.
 * In many cases these are simple thunk functions that translate checked exceptions
 * into unchecked errors in the same way a JVM will for reflective calls it makes
 * (e.g. Class.forName() to resolve references in bytecode).
 * @author robinsalkeld
 */
public class HolographInternalUtils {

    public static ObjectMirror getField(InstanceMirror o, String name) {
        try {
            return o.getMemberField(name).get();
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
    }

    public static void setField(InstanceMirror o, String name, ObjectMirror value) {
        try {
            o.getMemberField(name).set(value);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
    }

    public static void setField(InstanceMirror o, String name, int value) {
        try {
            o.getMemberField(name).setInt(value);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
    }

    public static ClassMirror loadClassMirrorInternal(ClassMirror context, String name) {
        return loadClassMirrorInternal(context.getVM(), context.getLoader(), name);
    }

    public static ClassMirror loadClassMirrorInternal(VirtualMachineMirror vm, ClassMirrorLoader loader, String name) {
        try {
            return Reflection.classMirrorForName(vm, ThreadHolograph.currentThreadMirror(), name, false, loader);
        } catch (ClassNotFoundException e) {
            NoClassDefFoundError error = new NoClassDefFoundError(e.getMessage());
            error.initCause(e);
            throw error;
        }
    }

    public static MethodMirror getMethod(ClassMirror klass, String name, ClassMirror... parameterTypes) {
        try {
            return klass.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            NoSuchMethodError error = new NoSuchMethodError(e.getMessage());
            error.initCause(e);
            throw error;
        }
    }

    public static ConstructorMirror getConstructor(ClassMirror klass, ClassMirror... parameterTypes) {
        try {
            return klass.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            NoSuchMethodError error = new NoSuchMethodError(e.getMessage());
            error.initCause(e);
            throw error;
        }
    }

    public static Object mirrorInvoke(ThreadMirror thread, MethodMirror method, ObjectMirror obj, Object... args) {
        try {
            return method.invoke(thread, obj, args);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IllegalAccessException e) {
            IllegalAccessError error = new IllegalAccessError(e.getMessage());
            error.initCause(e);
            throw error;
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static InstanceMirror newInstance(ConstructorMirror constructor, ThreadMirror thread, Object... args) {
        try {
            return constructor.newInstance(thread, args);
        } catch (IllegalAccessException e) {
            IllegalAccessError error = new IllegalAccessError(e.getMessage());
            error.initCause(e);
            throw error;
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            InstantiationError error = new InstantiationError(e.getMessage());
            error.initCause(e);
            throw error;
        }
    }

    
     
    
}
