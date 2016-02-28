package edu.ubc.aspects;

import java.util.concurrent.atomic.AtomicInteger;

privileged aspect AroundThreadLocals2 {
//    
//    private static AtomicInteger nextHashCodeForNewThreadLocals = new AtomicInteger();
//    
//    int around(): execution(int ThreadLocal.nextHashCode()) {
//        return nextHashCodeForNewThreadLocals.getAndIncrement();
//    }
}
