package edu.ubc.mirrors.holographs;

import java.util.List;

import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorEntryEvent;
import edu.ubc.mirrors.ConstructorMirrorExitEvent;
import edu.ubc.mirrors.ConstructorMirrorExitRequest;
import edu.ubc.mirrors.ConstructorMirrorHandlerEvent;
import edu.ubc.mirrors.ConstructorMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ThreadMirror;

public class ConstructorHolographHandlerEvent implements ConstructorMirrorHandlerEvent, MirrorInvocationHandler {

    private final VirtualMachineHolograph vm;
    private final ConstructorMirrorHandlerRequest request;
    private final ConstructorMirrorEntryEvent entryEvent;
    private final ConstructorMirrorExitRequest exitRequest;
    
    public ConstructorHolographHandlerEvent(VirtualMachineHolograph vm, ConstructorMirrorHandlerRequest request, ConstructorMirrorEntryEvent entryEvent, ConstructorMirrorExitRequest exitRequest) {
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
    public ConstructorMirror constructor() {
        return entryEvent.constructor();
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
            ConstructorMirrorExitEvent exitEvent = (ConstructorMirrorExitEvent)vm.dispatch().runUntil(exitRequest);
            return null;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
