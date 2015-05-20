package edu.ubc.aspects;

import java.util.HashMap;
import java.util.Map;

privileged aspect AroundThreadLocals perthis(execution(* ThreadLocal.*(..))) {
    
    private final Map<Thread, Object> newThreadLocalValues = new HashMap<Thread, Object>();
    
    // TODO-RS: Need to distinguish old thread locals from new
    
    Object around(Object tlo): execution(Object ThreadLocal.get()) && this(tlo) {
        Thread t = Thread.currentThread();
        ThreadLocal<Object> tl = ((ThreadLocal<Object>)tlo);
        if (newThreadLocalValues.containsKey(t)) {
            return newThreadLocalValues.get(t);
        } else {
            Object value = tl.initialValue();
            newThreadLocalValues.put(t, value);
            return value;
        }
    }
    
    void around(Object tlo, Object value): execution(void ThreadLocal.set(Object)) && this(tlo) && args(value) {
        Thread t = Thread.currentThread();
        newThreadLocalValues.put(t, value);
    }
}
