package edu.ubc.mirrors.holographs;

import java.util.List;

import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryEvent;
import edu.ubc.mirrors.MethodMirrorExitEvent;
import edu.ubc.mirrors.MethodMirrorExitRequest;
import edu.ubc.mirrors.MethodMirrorHandlerEvent;
import edu.ubc.mirrors.MethodMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ThreadMirror;

public class MethodHolographHandlerEvent implements MethodMirrorHandlerEvent, MirrorInvocationHandler {

    private final VirtualMachineHolograph vm;
    private final MethodMirrorHandlerRequest request;
    private final MethodMirrorEntryEvent entryEvent;
    private final MethodMirrorExitRequest exitRequest;
    
    public MethodHolographHandlerEvent(VirtualMachineHolograph vm, MethodMirrorHandlerRequest request, MethodMirrorEntryEvent entryEvent, MethodMirrorExitRequest exitRequest) {
        this.vm = vm;
        this.request = request;
        this.entryEvent = entryEvent;
        this.exitRequest = exitRequest;
        
    }
    
    @Override
    public MirrorEventRequest request() {
        return request;
    }
    
    @Override
    public ThreadMirror thread() {
        return entryEvent.thread();
    }
    
    @Override
    public MethodMirror method() {
        return entryEvent.method();
    }
    
    @Override
    public List<Object> arguments() {
        return entryEvent.arguments();
    }
    
    @Override
    public MirrorInvocationHandler proceed() {
        return this;
    }
    
    @Override
    public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
        // TODO-RS: Check thread
        // TODO-RS: Check arguments
        // TODO-RS: Track how many times this is called
        
        try {
            MethodMirrorExitEvent exitEvent = (MethodMirrorExitEvent)vm.dispatch().runUntil(exitRequest);
            return exitEvent.returnValue();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
