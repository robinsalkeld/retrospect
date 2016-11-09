package edu.ubc.mirrors.fieldmap;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorHandlerEvent;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;

public class DirectAdviceMirrorHandlerEvent implements MirrorEvent {

    private final Set<MirrorEventRequest> requests;
    private final ThreadMirror thread;
    private final List<Object> arguments;
    private MirrorInvocationHandler proceed;
    
    public DirectAdviceMirrorHandlerEvent(MirrorEventRequest request, ThreadMirror thread, List<Object> arguments, MirrorInvocationHandler proceed) {
        this.requests = new HashSet<MirrorEventRequest>();
        this.requests.add(request);
        this.thread = thread;
        this.arguments = arguments;
        this.proceed = proceed;
    }

    @Override
    public Set<MirrorEventRequest> requests() {
        return requests;
    }
    
    @Override
    public ThreadMirror thread() {
        return thread;
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
        return getClass().getSimpleName();
    }
}
