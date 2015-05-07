package edu.ubc.aspects;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

public aspect ShutdownHooks {
    
    // Augments ApplicationShutdownHooks.hooks
    private static IdentityHashMap<Thread, Thread> moreHooks 
             = new IdentityHashMap<Thread, Thread>();
    
    // Add retroactive hooks to moreHooks instead
    void around(Thread hook): cflow(execution(void ApplicationShutdownHooks.add(..))) 
                              && execution(void IdentityHashMap.put(..)) 
                              && args(hook) {
        moreHooks.put(hook, hook);
    }
    
    // When hooks is read, merge in moreHooks as well
    Set<Thread> around(): cflow(execution(void ApplicationShutdownHooks.runHooks(..))) 
                          && execution(void IdentityHashMap.keySet(..)) {
        Set<Thread> result = new HashSet<Thread>(proceed());
        result.addAll(moreHooks.keySet());
        return result;
    }
}
