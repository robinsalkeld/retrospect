package edu.ubc.mirrors.holographs;

import java.util.List;

import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorExitEvent;
import edu.ubc.mirrors.ConstructorMirrorExitRequest;
import edu.ubc.mirrors.ConstructorMirrorHandlerEvent;
import edu.ubc.mirrors.ConstructorMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;

public class ConstructorHolographHandlerEvent extends HolographEvent implements ConstructorMirrorHandlerEvent, MirrorInvocationHandler {

    private final VirtualMachineHolograph vm;
    private final ThreadMirror thread;
    private final ConstructorMirror constructor;
    private boolean isConstructorChaining;
    private final List<Object> arguments;
    private MirrorInvocationHandler proceed;
    private final ConstructorMirrorExitRequest exitRequest;
    
    public ConstructorHolographHandlerEvent(VirtualMachineHolograph vm, ConstructorMirrorHandlerRequest request, ThreadMirror thread, ConstructorMirror constructor, 
            boolean isConstructorChaining, List<Object> arguments) {
        super(request);
        this.vm = vm;
        this.thread = thread;
        this.constructor = constructor;
        this.isConstructorChaining = isConstructorChaining;
        this.arguments = arguments;
        this.proceed = this;
        this.exitRequest = vm.eventRequestManager().createConstructorMirrorExitRequest();
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
    public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
        // TODO-RS: Check thread
        // TODO-RS: Check arguments
        // TODO-RS: Track how many times this is called
        
        try {
            ConstructorHolograph constructor = (ConstructorHolograph)constructor();
            exitRequest.setConstructorFilter(constructor.getWrapped());
            exitRequest.enable();
            ConstructorMirrorExitEvent exitEvent = (ConstructorMirrorExitEvent)vm.dispatch().runUntil(exitRequest);
            exitRequest.disable();
            return exitEvent.returnValue();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + Reflection.constructorName(constructor);
    }
}
