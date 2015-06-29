package edu.ubc.mirrors.fieldmap;

import java.util.List;

import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorEntryEvent;
import edu.ubc.mirrors.ConstructorMirrorEntryRequest;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryEvent;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.ThreadMirror;

public class DirectConstructorMirrorEntryEvent implements ConstructorMirrorEntryEvent {

    private final ConstructorMirrorEntryRequest request;
    private final ThreadMirror thread;
    private final ConstructorMirror constructor;
    private final List<Object> arguments;
    private MirrorInvocationHandler proceed;
    private final boolean isConstructorChaining;
    
    public DirectConstructorMirrorEntryEvent(ConstructorMirrorEntryRequest request, ThreadMirror thread, ConstructorMirror constructor, List<Object> arguments, 
            MirrorInvocationHandler proceed, boolean isConstructorChaining) {
        this.request = request;
        this.thread = thread;
        this.constructor = constructor;
        this.arguments = arguments;
        this.proceed = proceed;
        this.isConstructorChaining = isConstructorChaining;
    }

    @Override
    public MirrorEventRequest request() {
        return request;
    }
    
    @Override
    public ThreadMirror thread() {
        return thread;
    }
    
    @Override
    public ConstructorMirror constructor() {
        return constructor;
    }
    
    @Override
    public List<Object> arguments() {
        return arguments;
    }
    
    @Override
    public MirrorInvocationHandler getProceed() {
        return proceed;
    }
    
    @Override
    public void setProceed(MirrorInvocationHandler proceed) {
        this.proceed = proceed;
    }
    
    @Override
    public boolean isConstructorChaining() {
        return isConstructorChaining;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + constructor();
    }
}
