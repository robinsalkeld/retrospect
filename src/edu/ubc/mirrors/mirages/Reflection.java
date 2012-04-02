package edu.ubc.mirrors.mirages;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.raw.NativeObjectMirror;

public class Reflection {

    public static ObjectMirror getMirror(Object o) {
        if (o instanceof Mirage) {
            return ((Mirage)o).getMirror();
        } else {
            return NativeObjectMirror.makeMirror(o);
        }
    }
    
    public static Object invoke(Object object, String name, Object... args) {
        Method match = null;
        for (Method m : object.getClass().getMethods()) {
            // TODO: Also check param types
            if (!m.isSynthetic() && m.getName().equals(name) && m.getParameterTypes().length == args.length) {
                if (match != null) {
                    throw new IllegalArgumentException("Ambiguous method name: " + object.getClass().getName() + "#" + name);
                }
                match = m;
            }
        }
        if (match == null) {
            throw new IllegalArgumentException("Method not found: " + object.getClass().getName() + "#" + name);
        }
        
        try {
            return match.invoke(object, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        
    }
    
    public static Object invoke(Class<?> klass, String name, Object... args) {
        Method match = null;
        for (Method m : klass.getMethods()) {
            // TODO: Also check param types
            if (m.getName().equals(name) && m.getParameterTypes().length == args.length) {
                if (match != null) {
                    throw new IllegalArgumentException("Ambiguous method name: " + klass.getName() + "#" + name);
                }
                match = m;
            }
        }
        if (match == null) {
            throw new IllegalArgumentException("Method not found: " + klass.getName() + "#" + name);
        }
        
        try {
            return match.invoke(null, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
}
