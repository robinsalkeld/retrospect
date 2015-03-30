package edu.ubc.mirrors.holographs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ubc.mirrors.Callback;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryEvent;
import edu.ubc.mirrors.MethodMirrorEntryRequest;
import edu.ubc.mirrors.MethodMirrorExitRequest;
import edu.ubc.mirrors.MethodMirrorHandlerEvent;
import edu.ubc.mirrors.MethodMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEvent;

public class MethodHolographHandlerRequest implements MethodMirrorHandlerRequest {

    private final VirtualMachineHolograph vm;
    private final Map<Object, Object> properties = new HashMap<Object, Object>();
    private final MethodMirrorEntryRequest entryRequest;
    private final MethodMirrorExitRequest exitRequest;
    private List<String> classNamePatterns = new ArrayList<String>();
    private MethodMirror methodFilter;
    
    private final Callback<MirrorEvent> entryCallback = new Callback<MirrorEvent>() {
        @Override
        public Object handle(MirrorEvent t) {
            MethodMirrorEntryEvent entryEvent = (MethodMirrorEntryEvent)t;
            MethodMirrorHandlerEvent handlerEvent = new MethodHolographHandlerEvent(vm, MethodHolographHandlerRequest.this, entryEvent, exitRequest);
            vm.dispatch().handleEvent(handlerEvent);
            vm.dispatch().handleSetEvent();
            return null;
        }
    };
    
    public MethodHolographHandlerRequest(VirtualMachineHolograph vm) {
        this.vm = vm;
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
        return properties.get(key);
    }

    @Override
    public void putProperty(Object key, Object value) {
        properties.put(key, value);
    }

    @Override
    public void addClassFilter(String classNamePattern) {
        classNamePatterns.add(classNamePattern);
        entryRequest.addClassFilter(classNamePattern);
        exitRequest.addClassFilter(classNamePattern);
    }
    
    @Override
    public void setMethodFilter(MethodMirror method) {
        this.methodFilter = method;
        entryRequest.setMethodFilter(method);
        exitRequest.setMethodFilter(method);
    }
    
    public MethodMirror getMethodFilter() {
        return methodFilter;
    }
    
    public boolean matches(MethodMirror method) {
        if (!classNamePatterns.isEmpty()) {
            if (!classNamePatterns.contains(method.getDeclaringClass().getClassName())) {
                return false;
            }
        }
        
        if (methodFilter != null) {
            if (methodFilter != method) {
                return false;
            }
        }
        
        return true;
    }
}
