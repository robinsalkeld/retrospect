package edu.ubc.mirrors.holographs;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ubc.mirrors.Callback;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorEntryEvent;
import edu.ubc.mirrors.ConstructorMirrorEntryRequest;
import edu.ubc.mirrors.ConstructorMirrorExitEvent;
import edu.ubc.mirrors.ConstructorMirrorExitRequest;
import edu.ubc.mirrors.ConstructorMirrorHandlerEvent;
import edu.ubc.mirrors.ConstructorMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ThreadMirror;

public class ConstructorHolographHandlerRequest implements ConstructorMirrorHandlerRequest, MirrorInvocationHandler {

    private final VirtualMachineHolograph vm;
    private final Map<Object, Object> properties = new HashMap<Object, Object>();
    private final ConstructorMirrorEntryRequest entryRequest;
    private final ConstructorMirrorExitRequest exitRequest;
    
    private final Callback<MirrorEvent> entryCallback = new Callback<MirrorEvent>() {
        @Override
        public MirrorEvent handle(MirrorEvent t) {
            ConstructorMirrorEntryEvent entryEvent = (ConstructorMirrorEntryEvent)t;
            final ConstructorMirrorHandlerEvent handlerEvent = new ConstructorHolographHandlerEvent(ConstructorHolographHandlerRequest.this, 
                    entryEvent.thread(), entryEvent.constructor(), entryEvent.arguments(), ConstructorHolographHandlerRequest.this);
            
            t.setProceed(new MirrorInvocationHandler() {
                public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
                    return vm.dispatch().runCallbacks(Collections.<MirrorEvent>singleton(handlerEvent));
                }
            });
            return t;
        }
    };
    
    public ConstructorHolographHandlerRequest(VirtualMachineHolograph vm) {
        this.vm = vm;
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
        return properties.get(key);
    }

    @Override
    public void putProperty(Object key, Object value) {
        properties.put(key, value);
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
