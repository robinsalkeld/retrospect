package edu.ubc.aspects;

import java.util.Collection;
import java.util.IdentityHashMap;

public aspect ShutdownHooks {
    
    // Augments ApplicationShutdownHooks.hooks
    private static IdentityHashMap<Thread, Thread> moreHooks 
             = new IdentityHashMap<Thread, Thread>();
    
    // Add retroactive hooks to moreHooks instead
    void around(Thread hook): execution(void Runtime.addShutdownHook(Thread)) && args(hook) {
        moreHooks.put(hook, hook);
    }
    
    // When hooks is read, merge in moreHooks as well
    after(): execution(void Shutdown.runHooks()) {
        // Copy of ApplicationShutdownHooks.runHooks()
        Collection<Thread> threads;
        synchronized(ShutdownHooks.class) {
            threads = moreHooks.keySet();
            moreHooks = null;
        }

        for (Thread hook : threads) {
            hook.start();
        }
        for (Thread hook : threads) {
            try {
                hook.join();
            } catch (InterruptedException x) { }
        }
    }
}
