package edu.ubc.aspects;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

aspect AroundThreadLocals {//perthis(execution(* ThreadLocal.*(..))) {
    
    private final Map<Thread, Object> newThreadLocalValues = new HashMap<Thread, Object>();
    
    // TODO-RS: Need to distinguish old thread locals from new
    
    private static final Method initialValueMethod;
    static {
        try {
            initialValueMethod = ThreadLocal.class.getDeclaredMethod("initialValue");
            initialValueMethod.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }
    
    Object around(Object tlo): execution(* ThreadLocal.get()) && this(tlo) {
        Thread t = Thread.currentThread();
        ThreadLocal<Object> tl = ((ThreadLocal<Object>)tlo);
        if (newThreadLocalValues.containsKey(t)) {
            return newThreadLocalValues.get(t);
        } else {
            // Would be direct access but we don't support privileged aspects
            try {
                Object value = initialValueMethod.invoke(tl);
                newThreadLocalValues.put(t, value);
                return value;
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    void around(Object tlo, Object value): execution(void ThreadLocal.set(*)) && this(tlo) && args(value) {
        Thread t = Thread.currentThread();
        newThreadLocalValues.put(t, value);
    }
}
