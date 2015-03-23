package edu.ubc.mirrors.holograms;

import java.util.List;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorHandlerEvent;
import edu.ubc.mirrors.MethodMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ThreadMirror;

public class MethodHologramHandlerEvent implements MethodMirrorHandlerEvent, MirrorInvocationHandler {

    private final MethodMirrorHandlerRequest request;
    private final ThreadMirror thread;
    private final MethodMirror method;
    private final List<Object> arguments;
    
    public MethodHologramHandlerEvent(MethodMirrorHandlerRequest request, ThreadMirror thread, MethodMirror method, List<Object> arguments) {
        this.request = request;
        this.thread = thread;
        this.method = method;
        this.arguments = arguments;
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
    public MirrorInvocationHandler proceed() {
        return this;
    }
    
    @Override
    public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
        
        InstanceMirror obj = (InstanceMirror)args.get(0);
        try {
            return method.invoke(thread, obj, args.subList(1, args.size()));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
