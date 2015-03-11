package edu.ubc.retrospect;

import org.aspectj.runtime.internal.AroundClosure;

/**
 * Class dynamically loaded into the aspect class loader 
 * to hold onto a AroundClosureMirror opaquely.
 * 
 * @author robinsalkeld
 */
public class MirrorInvocationHandlerAroundClosure extends AroundClosure {

    private final Object /* AroundClosureMirror */ handler;
    
    public MirrorInvocationHandlerAroundClosure(Object handler) {
        super();
        this.handler = handler;
    }

    @Override
    public native Object run(Object[] args) throws Throwable;
}
