package edu.ubc.mirrors.holograms;

import java.util.List;

import edu.ubc.mirrors.InvocableMirrorEvent;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorHandlerEvent;
import edu.ubc.mirrors.MethodMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.ThreadMirror;

public class MethodHolographHandlerEvent implements MethodMirrorHandlerEvent {

    private final MethodMirrorHandlerRequest request;
    private final ThreadMirror thread;
    private final MethodMirror method;
    private final List<Object> arguments;
    private MirrorInvocationHandler proceed;
    
    public MethodHolographHandlerEvent(MethodMirrorHandlerRequest request, ThreadMirror thread, MethodMirror method, List<Object> arguments, MirrorInvocationHandler proceed) {
        this.request = request;
        this.thread = thread;
        this.method = method;
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
    public MethodMirror method() {
        return method;
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
}