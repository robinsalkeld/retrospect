package edu.ubc.mirrors.holographs;

import java.util.List;

import edu.ubc.mirrors.Callback;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryEvent;
import edu.ubc.mirrors.MethodMirrorEntryRequest;
import edu.ubc.mirrors.MethodMirrorExitEvent;
import edu.ubc.mirrors.MethodMirrorExitRequest;
import edu.ubc.mirrors.MethodMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;

public class MethodHolographHandlerRequest implements MethodMirrorHandlerRequest, MirrorInvocationHandler {

    private final VirtualMachineHolograph vm;
    private final MirrorInvocationHandler handler;
    private final MethodMirrorEntryRequest entryRequest;
    private final MethodMirrorExitRequest exitRequest;
    
    private final Callback<MirrorEvent> entryCallback = new Callback<MirrorEvent>() {
        @Override
        public void handle(MirrorEvent t) {
            MethodMirrorEntryEvent entryEvent = (MethodMirrorEntryEvent)t;
            try {
                handler.invoke(entryEvent.arguments(), MethodHolographHandlerRequest.this);
                // TODO-RS: Check result value
            } catch (MirrorInvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    };
    
    public MethodHolographHandlerRequest(VirtualMachineHolograph vm, MirrorInvocationHandler handler) {
        this.vm = vm;
        this.handler = handler;
        this.entryRequest = vm.eventRequestManager().createMethodMirrorEntryRequest();
        vm.dispatch().addCallback(entryRequest, entryCallback); 
        
        this.exitRequest = vm.eventRequestManager().createMethodMirrorExitRequest();
        
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
    public void setMethodFilter(MethodMirror method) {
        entryRequest.setMethodFilter(method);
        exitRequest.setMethodFilter(method);
    }

    public Object invoke(List<Object> args, MirrorInvocationHandler original) throws MirrorInvocationTargetException {
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
