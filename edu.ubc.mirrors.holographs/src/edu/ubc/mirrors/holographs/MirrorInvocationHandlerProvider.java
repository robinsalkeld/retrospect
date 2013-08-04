package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.MethodMirror;


/**
 * Plugin interface for providing mirror-based native method (MNM) implementations.
 * 
 * @author robinsalkeld
 */
public interface MirrorInvocationHandlerProvider {

    /**
     * Retrieves the handler for the given method, or <code>null</code>
     * if this provider doesn't handle the given method.
     */
    public MirrorInvocationHandler getInvocationHandler(MethodMirror method);
}
