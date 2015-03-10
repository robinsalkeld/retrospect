package edu.ubc.mirrors.holographs;

import java.util.List;

import edu.ubc.mirrors.Callback;
import edu.ubc.mirrors.InvocableMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorEntryEvent;
import edu.ubc.mirrors.ConstructorMirrorEntryRequest;
import edu.ubc.mirrors.ConstructorMirrorExitEvent;
import edu.ubc.mirrors.ConstructorMirrorExitRequest;
import edu.ubc.mirrors.ConstructorMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ThreadMirror;

public class ConstructorHolographHandlerRequest implements ConstructorMirrorHandlerRequest, MirrorInvocationHandler {

    private final VirtualMachineHolograph vm;
    private final MirrorInvocationHandler handler;
    private final ConstructorMirrorEntryRequest entryRequest;
    private final ConstructorMirrorExitRequest exitRequest;
    
    private final Callback<MirrorEvent> entryCallback = new Callback<MirrorEvent>() {
        @Override
        public Object handle(MirrorEvent t) {
            ConstructorMirrorEntryEvent entryEvent = (ConstructorMirrorEntryEvent)t;
            try {
                Object result = handler.invoke(entryEvent.thread(), entryEvent.constructor(), entryEvent.arguments(), ConstructorHolographHandlerRequest.this);
                // TODO-RS: Check result value
                return result;
            } catch (MirrorInvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    };
    
    public ConstructorHolographHandlerRequest(VirtualMachineHolograph vm, MirrorInvocationHandler handler) {
        this.vm = vm;
        this.handler = handler;
        this.entryRequest = vm.eventRequestManager().createConstructorMirrorEntryRequest();
        vm.dispatch().addCallback(entryRequest, entryCallback); 
        
        this.exitRequest = vm.eventRequestManager().createConstructorMirrorExitRequest();
        
    }

    @Override
    public void enable() {
        entryRequest.enable();
        exitRequest.enable();
    }

    @Override
    public void disable() {
        entryRequest.enable();
        exitRequest.enable();
    }

    @Override
    public boolean isEnabled() {
        return entryRequest.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        entryRequest.setEnabled(enabled);
        exitRequest.setEnabled(enabled);
    }

    @Override
    public Object getProperty(Object key) {
        return entryRequest.getProperty(key);
    }

    @Override
    public void putProperty(Object key, Object value) {
        entryRequest.putProperty(key, value);
    }

    @Override
    public void addClassFilter(String classNamePattern) {
        entryRequest.addClassFilter(classNamePattern);
        exitRequest.addClassFilter(classNamePattern);
    }
    
    @Override
    public void setConstructorFilter(ConstructorMirror method) {
        entryRequest.setConstructorFilter(method);
        exitRequest.setConstructorFilter(method);
    }

    public Object invoke(ThreadMirror thread, InvocableMirror invocable, List<Object> args, MirrorInvocationHandler original) throws MirrorInvocationTargetException {
        // TODO-RS: Check thread
        // TODO-RS: Check invocable
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
