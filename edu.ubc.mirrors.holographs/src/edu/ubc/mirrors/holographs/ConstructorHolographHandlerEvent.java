package edu.ubc.mirrors.holographs;

import java.util.List;

import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorHandlerEvent;
import edu.ubc.mirrors.ConstructorMirrorHandlerRequest;
import edu.ubc.mirrors.InvocableMirrorEvent;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;

public class ConstructorHolographHandlerEvent implements ConstructorMirrorHandlerEvent {

    private final ConstructorMirrorHandlerRequest request;
    private final ThreadMirror thread;
    private final ConstructorMirror constructor;
    private boolean isConstructorChaining;
    private final List<Object> arguments;
    private MirrorInvocationHandler proceed;
    
    public ConstructorHolographHandlerEvent(ConstructorMirrorHandlerRequest request, ThreadMirror thread, ConstructorMirror constructor, 
            boolean isConstructorChaining, List<Object> arguments, MirrorInvocationHandler proceed) {
        this.request = request;
        this.thread = thread;
        this.constructor = constructor;
        this.isConstructorChaining = isConstructorChaining;
        this.arguments = arguments;
        this.proceed = proceed;
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
    public boolean isConstructorChaining() {
        return isConstructorChaining;
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
    public String toString() {
        return getClass().getSimpleName() + " on " + Reflection.constructorName(constructor);
    }
}
