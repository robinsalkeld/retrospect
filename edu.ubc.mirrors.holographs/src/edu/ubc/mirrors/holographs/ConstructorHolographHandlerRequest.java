package edu.ubc.mirrors.holographs;

import java.util.HashMap;
import java.util.Map;

import edu.ubc.mirrors.Callback;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorEntryEvent;
import edu.ubc.mirrors.ConstructorMirrorEntryRequest;
import edu.ubc.mirrors.ConstructorMirrorHandlerEvent;
import edu.ubc.mirrors.ConstructorMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.Reflection;

public class ConstructorHolographHandlerRequest implements ConstructorMirrorHandlerRequest {

    private final VirtualMachineHolograph vm;
    private final Map<Object, Object> properties = new HashMap<Object, Object>();
    private ConstructorMirror contructorFilter;
    private final ConstructorMirrorEntryRequest entryRequest;
    
    private final Callback<MirrorEvent> entryCallback = new Callback<MirrorEvent>() {
        @Override
        public MirrorEvent handle(MirrorEvent t) {
            ConstructorMirrorEntryEvent entryEvent = (ConstructorMirrorEntryEvent)t;
            final ConstructorMirrorHandlerEvent handlerEvent = new ConstructorHolographHandlerEvent(vm, ConstructorHolographHandlerRequest.this, 
                    entryEvent.thread(), entryEvent.constructor(), entryEvent.isConstructorChaining(), entryEvent.arguments());
            
            vm.dispatch().raiseEvent(handlerEvent);
            return t;
        }
    };
    
    public ConstructorHolographHandlerRequest(VirtualMachineHolograph vm) {
        this.vm = vm;
        this.entryRequest = vm.eventRequestManager().createConstructorMirrorEntryRequest();
        vm.dispatch().addCallback(entryRequest, entryCallback); 
    }

    @Override
    public void enable() {
        entryRequest.enable();
    }

    @Override
    public void disable() {
        entryRequest.disable();
    }

    @Override
    public boolean isEnabled() {
        return entryRequest.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        entryRequest.setEnabled(enabled);
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
    }
    
    @Override
    public void setConstructorFilter(ConstructorMirror contructorFilter) {
        this.contructorFilter = contructorFilter;
        entryRequest.setConstructorFilter(contructorFilter);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " for " + Reflection.constructorName(contructorFilter);
    }
}
