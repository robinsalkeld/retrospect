package edu.ubc.mirrors.fieldmap;

import java.util.List;

import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorExitEvent;
import edu.ubc.mirrors.ConstructorMirrorExitRequest;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorExitEvent;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.ThreadMirror;

public class DirectConstructorMirrorExitEvent implements ConstructorMirrorExitEvent {

    private final ConstructorMirrorExitRequest request;
    private final ThreadMirror thread;
    private final ConstructorMirror constructor;
    private final List<Object> arguments;
    private MirrorInvocationHandler proceed;
    private final InstanceMirror result;
    
    public DirectConstructorMirrorExitEvent(ConstructorMirrorExitRequest request, ThreadMirror thread, ConstructorMirror constructor, List<Object> arguments, 
            MirrorInvocationHandler proceed, InstanceMirror result) {
        this.request = request;
        this.thread = thread;
        this.constructor = constructor;
        this.arguments = arguments;
        this.proceed = proceed;
        this.result = result;
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
    public InstanceMirror returnValue() {
        return result;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + constructor();
    }
}
