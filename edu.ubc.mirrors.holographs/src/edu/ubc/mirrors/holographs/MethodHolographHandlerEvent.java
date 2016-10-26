package edu.ubc.mirrors.holographs;

import java.util.List;

import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorExitEvent;
import edu.ubc.mirrors.MethodMirrorExitRequest;
import edu.ubc.mirrors.MethodMirrorHandlerEvent;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;

public class MethodHolographHandlerEvent extends HolographEvent implements MethodMirrorHandlerEvent, MirrorInvocationHandler {

    private final VirtualMachineHolograph vm;
    private final ThreadMirror thread;
    private final MethodMirror method;
    private final List<Object> arguments;
    private MirrorInvocationHandler proceed;
    private final MethodMirrorExitRequest exitRequest;
    
    public MethodHolographHandlerEvent(VirtualMachineHolograph vm, MirrorEventRequest request, ThreadMirror thread, 
            MethodMirror method, List<Object> arguments) {
        super(request);
        this.vm = vm;
        this.thread = thread;
        this.method = method;
        this.arguments = arguments;
        this.proceed = this;
        this.exitRequest = vm.eventRequestManager().createMethodMirrorExitRequest();
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
    
    @Override
    public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
        // TODO-RS: Check thread
        // TODO-RS: Check arguments
        // TODO-RS: Track how many times this is called
        
        try {
            exitRequest.setMethodFilter(method.getDeclaringClass().getClassName(), method.getName(), method.getParameterTypeNames());
            exitRequest.enable();
            MethodMirrorExitEvent exitEvent = (MethodMirrorExitEvent)vm.dispatch().runUntil(exitRequest);
            exitRequest.disable();
            return exitEvent.returnValue();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " for " + Reflection.methodName(method);
    }
}
