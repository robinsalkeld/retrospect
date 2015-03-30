package edu.ubc.mirrors.holographs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.ubc.mirrors.ConstructorMirrorHandlerRequest;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FieldMirrorSetHandlerRequest;
import edu.ubc.mirrors.InvocableMirrorEvent;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.holograms.MethodHolographHandlerEvent;
import edu.ubc.mirrors.wrapping.WrappingMirrorEventRequestManager;

public class HolographEventRequestManager extends WrappingMirrorEventRequestManager {

    private final VirtualMachineHolograph vm;
    
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
    public FieldMirrorSetHandlerRequest createFieldMirrorSetHandlerRequest(FieldMirror field) {
        FieldHolographSetHandlerRequest request = new FieldHolographSetHandlerRequest(vm, field);
        fieldSetHandlerRequests.add(request);
        return request;
    }
    
//    public List<FieldHologramSetEvent> requestsForFieldSet(FieldMirror field) {
//        List<FieldHologramSetEvent> handlers = new ArrayList<FieldHologramSetEvent>();
//        for (FieldHologramSetEvent request : fieldSetHandlerRequests) {
//            if (request.get.equals(field)) {
//                handlers.add(request);
//            }
//        }
//        return handlers;
//    }
    
    public Object handleMethodInvocation(MirrorInvocationHandler original, MethodMirror method, List<Object> arguments) throws MirrorInvocationTargetException {
        MirrorInvocationHandler handler = original;
        for (MethodHolographHandlerRequest request : methodHandlerRequests) {
            if (request.matches(method)) {
                MethodHolographHandlerEvent event = new MethodHolographHandlerEvent(request, ThreadHolograph.currentThreadMirror(), method, arguments, handler);
                handler = vm.dispatch().handleInvocableEvent(event);
            }
        }
        
        return handler.invoke(ThreadHolograph.currentThreadMirror(), arguments);
    }

    public boolean methodRequested(MethodMirror method) {
        for (MethodHolographHandlerRequest request : methodHandlerRequests) {
            if (request.matches(method)) {
                return true;
            }
        }
        return false;
    }
}

