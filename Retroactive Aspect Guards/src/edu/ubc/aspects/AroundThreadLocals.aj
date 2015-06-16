package edu.ubc.aspects;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

aspect AroundThreadLocals {
    
    private final Map<ThreadLocal, Map<Thread, Object>> newThreadLocalValues 
            = new HashMap<ThreadLocal, Map<Thread, Object>>();
    
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
    
    private Map<Thread, Object> threadLocalMap(ThreadLocal threadLocal) {
        Map<Thread, Object> result = newThreadLocalValues.get(threadLocal);
        if (result == null) {
            result = new HashMap<Thread, Object>();
            newThreadLocalValues.put(threadLocal, result);
        }
        return result;
    }
    
    Object around(Object tlo): execution(* ThreadLocal.get()) && this(tlo) {
        Thread t = Thread.currentThread();
        ThreadLocal<Object> tl = ((ThreadLocal<Object>)tlo);
        Map<Thread, Object> map = threadLocalMap(tl); 
        if (map.containsKey(t)) {
            return map.get(t);
        } else {
            // Would be direct access but we don't support privileged aspects
            try {
                Object value = initialValueMethod.invoke(tl);
                map.put(t, value);
                return value;
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    void around(Object tlo, Object value): execution(void ThreadLocal.set(*)) && this(tlo) && args(value) {
        Thread t = Thread.currentThread();
        ThreadLocal<Object> tl = ((ThreadLocal<Object>)tlo);
        threadLocalMap(tl).put(t, value);
    }
}
