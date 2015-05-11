package edu.ubc.mirrors.holographs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.ubc.mirrors.ConstructorMirrorHandlerRequest;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FieldMirrorGetHandlerRequest;
import edu.ubc.mirrors.FieldMirrorSetHandlerRequest;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.InvocableMirrorEvent;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.holograms.FieldGetProceed;
import edu.ubc.mirrors.holograms.FieldHologramSetEvent;
import edu.ubc.mirrors.holograms.FieldSetProceed;
import edu.ubc.mirrors.holograms.MethodHolographHandlerEvent;
import edu.ubc.mirrors.wrapping.WrappingMirrorEventRequestManager;

public class HolographEventRequestManager extends WrappingMirrorEventRequestManager {

    private final VirtualMachineHolograph vm;
    
    private final List<FieldHolographGetHandlerRequest> fieldGetHandlerRequests =
            new ArrayList<FieldHolographGetHandlerRequest>();
    private final List<FieldHolographSetHandlerRequest> fieldSetHandlerRequests =
            new ArrayList<FieldHolographSetHandlerRequest>();
    private final List<MethodHolographHandlerRequest> methodHandlerRequests =
            new ArrayList<MethodHolographHandlerRequest>();
    
    public HolographEventRequestManager(VirtualMachineHolograph vm, MirrorEventRequestManager wrapped) {
        super(vm, wrapped);
        this.vm = vm;
    }
    
    @Override
    public MethodMirrorHandlerRequest createMethodMirrorHandlerRequest() {
        MethodHolographHandlerRequest request = new MethodHolographHandlerRequest(vm);
        methodHandlerRequests.add(request);
        return request;
    }
    
    @Override
    public ConstructorMirrorHandlerRequest createConstructorMirrorHandlerRequest() {
        return new ConstructorHolographHandlerRequest(vm);
    }
    
    @Override
    public FieldMirrorGetHandlerRequest createFieldMirrorGetHandlerRequest(FieldMirror field) {
        FieldHolographGetHandlerRequest request = new FieldHolographGetHandlerRequest(vm, field);
        fieldGetHandlerRequests.add(request);
        return request;
    }
    
    @Override
    public FieldMirrorSetHandlerRequest createFieldMirrorSetHandlerRequest(FieldMirror field) {
        FieldHolographSetHandlerRequest request = new FieldHolographSetHandlerRequest(vm, field);
        fieldSetHandlerRequests.add(request);
        return request;
    }
    
    public Object handleMethodInvocation(MirrorInvocationHandler original, MethodMirror method, List<Object> arguments) throws MirrorInvocationTargetException {
        Set<MirrorEvent> events = new HashSet<MirrorEvent>();
        for (MethodHolographHandlerRequest request : methodHandlerRequests) {
            if (request.matches(method)) {
                events.add(new MethodHolographHandlerEvent(request, ThreadHolograph.currentThreadMirror(), method, arguments, original));
            }
        }
        
        if (events.isEmpty()) {
            return original.invoke(ThreadHolograph.currentThreadMirror(), arguments);
        } else {
            return vm.dispatch().runCallbacks(events);
        }
    }

    public boolean methodRequested(MethodMirror method) {
        for (MethodHolographHandlerRequest request : methodHandlerRequests) {
            if (request.matches(method)) {
                return true;
            }
        }
        return false;
    }
    
    public void handleFieldSet(final InstanceMirror target, final FieldMirror field, final Object newValue) throws IllegalAccessException, MirrorInvocationTargetException {
        MirrorInvocationHandler original = new FieldSetProceed(field);
        Set<MirrorEvent> events = new HashSet<MirrorEvent>();
        for (FieldHolographSetHandlerRequest request : fieldSetHandlerRequests) {
            if (request.matches(field)) {
                events.add(new FieldHolographSetHandlerEvent(request, ThreadHolograph.currentThreadMirror(), target, field, newValue, original));
            }
        }
        
        if (events.isEmpty()) {
            Reflection.setField(target, field, newValue);
        } else {
            vm.dispatch().runCallbacks(events);
        }
    }

    public Object handleFieldGet(InstanceMirror target, FieldMirror field) throws IllegalAccessException, MirrorInvocationTargetException {
        MirrorInvocationHandler original = new FieldGetProceed(field);
        Set<MirrorEvent> events = new HashSet<MirrorEvent>();
        for (FieldHolographGetHandlerRequest request : fieldGetHandlerRequests) {
            if (request.matches(field)) {
                events.add(new FieldHolographGetHandlerEvent(request, ThreadHolograph.currentThreadMirror(), target, field, original));
            }
        }
        
        if (events.isEmpty()) {
            return Reflection.getBoxedValue(target, field);
        } else {
            return vm.dispatch().runCallbacks(events);
        }
    }
}

