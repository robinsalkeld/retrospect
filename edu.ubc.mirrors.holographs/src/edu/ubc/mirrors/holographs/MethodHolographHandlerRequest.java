package edu.ubc.mirrors.holographs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ubc.mirrors.Callback;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryEvent;
import edu.ubc.mirrors.MethodMirrorEntryRequest;
import edu.ubc.mirrors.MethodMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.Reflection;

public class MethodHolographHandlerRequest implements MethodMirrorHandlerRequest {

    private final VirtualMachineHolograph vm;
    private final Map<Object, Object> properties = new HashMap<Object, Object>();
    private final MethodMirrorEntryRequest entryRequest;
    private boolean entryCallbackAdded = false;
    private List<String> classNamePatterns = new ArrayList<String>();
    protected String declaringClassFilter;
    protected String nameFilter;
    protected List<String> parameterTypeNamesFilter;
    
    private final Callback<MirrorEvent> entryCallback = new Callback<MirrorEvent>() {
        @Override
        public MirrorEvent handle(MirrorEvent t) {
            MethodMirrorEntryEvent entryEvent = (MethodMirrorEntryEvent)t;
            final MethodHolographHandlerEvent handlerEvent = new MethodHolographHandlerEvent(vm, MethodHolographHandlerRequest.this, 
                    entryEvent.thread(), entryEvent.method(), entryEvent.arguments());
            vm.dispatch().raiseEvent(handlerEvent);
            return t;
        }
    };
    
    public MethodHolographHandlerRequest(VirtualMachineHolograph vm) {
        this.vm = vm;
        this.entryRequest = vm.eventRequestManager().createMethodMirrorEntryRequest();
    }

    @Override
    public void enable() {
        entryRequest.enable();
        // Delaying the add until now for better debugging
        if (!entryCallbackAdded) {
            entryCallbackAdded = true;
            vm.checkAlreadyDefinedClassesForRequest(this);
            vm.dispatch().addCallback(entryRequest, entryCallback);
        }
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
        if (enabled) {
            enable();
        } else {
            disable();
        }
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
    }
    
    @Override
    public void setMethodFilter(String declaringClass, String name, List<String> parameterTypeNames) {
        this.declaringClassFilter = declaringClass;
        this.nameFilter = name;
        this.parameterTypeNamesFilter = parameterTypeNames;
        
        entryRequest.setMethodFilter(declaringClass, name, parameterTypeNames);
    }
    
    public boolean matches(MethodMirror method) {
        if (!classNamePatterns.isEmpty()) {
            if (!classNamePatterns.contains(method.getDeclaringClass().getClassName())) {
                return false;
            }
        }
        
        if (nameFilter != null) {
            if (!Reflection.methodMatches(method, declaringClassFilter, nameFilter, parameterTypeNamesFilter)) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + (nameFilter == null ? "" : " for " + nameFilter);
    }
}
