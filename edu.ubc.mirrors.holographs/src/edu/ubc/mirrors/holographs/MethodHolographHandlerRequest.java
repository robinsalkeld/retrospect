package edu.ubc.mirrors.holographs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.holograms.MethodHolographHandlerEvent;

public class MethodHolographHandlerRequest implements MethodMirrorHandlerRequest, MirrorInvocationHandler {

    private final VirtualMachineHolograph vm;
    private final Map<Object, Object> properties = new HashMap<Object, Object>();
    private final MethodMirrorEntryRequest entryRequest;
    private final MethodMirrorExitRequest exitRequest;
    private MethodMirrorExitEvent exitEvent;
    private List<String> classNamePatterns = new ArrayList<String>();
    protected String declaringClassFilter;
    protected String nameFilter;
    protected List<String> parameterTypeNamesFilter;
    
    private final Callback<MirrorEvent> entryCallback = new Callback<MirrorEvent>() {
        @Override
        public MirrorEvent handle(MirrorEvent t) {
            MethodMirrorEntryEvent entryEvent = (MethodMirrorEntryEvent)t;
            final MethodHolographHandlerEvent handlerEvent = new MethodHolographHandlerEvent(MethodHolographHandlerRequest.this, 
                    entryEvent.thread(), entryEvent.method(), entryEvent.arguments(), MethodHolographHandlerRequest.this);
            
            handlerEvent.setProceed(MethodHolographHandlerRequest.this);
            vm.dispatch().raiseEvent(handlerEvent);
            return t;
        }
    };
    
    public MethodHolographHandlerRequest(VirtualMachineHolograph vm) {
        this.vm = vm;
        this.entryRequest = vm.eventRequestManager().createMethodMirrorEntryRequest();
        vm.dispatch().addCallback(entryRequest, entryCallback); 
        
        this.exitRequest = vm.eventRequestManager().createMethodMirrorExitRequest();
        this.exitRequest.putProperty("for handler request", this);
        
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
    public void setMethodFilter(String declaringClass, String name, List<String> paramterTypeNames) {
        entryRequest.setMethodFilter(declaringClass, name, paramterTypeNames);
        exitRequest.setMethodFilter(declaringClass, name, paramterTypeNames);
    }
    
    public boolean matches(MethodMirror method) {
        if (!classNamePatterns.isEmpty()) {
            if (!classNamePatterns.contains(method.getDeclaringClass().getClassName())) {
                return false;
            }
        }
        
        if (nameFilter != null) {
            if (Reflection.methodMatches(method, declaringClassFilter, nameFilter, parameterTypeNamesFilter)) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
        // TODO-RS: Check thread
        // TODO-RS: Check arguments
        // TODO-RS: Track how many times this is called
        
        try {
            exitEvent = (MethodMirrorExitEvent)vm.dispatch().runUntil(exitRequest);
            return exitEvent.returnValue();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + (nameFilter == null ? "" : " " + nameFilter);
    }
}
