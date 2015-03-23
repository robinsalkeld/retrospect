package edu.ubc.mirrors.holographs;

import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.ConstructorMirrorHandlerRequest;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FieldMirrorSetHandlerRequest;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.holograms.FieldHologramSetEvent;
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
    
    public List<MethodHolographHandlerRequest> requestsForMethodMirror(MethodMirror method) {
        List<MethodHolographHandlerRequest> handlers = new ArrayList<MethodHolographHandlerRequest>();
        for (MethodHolographHandlerRequest request : methodHandlerRequests) {
            if (method.equals(request.getMethodFilter())) {
                handlers.add(request);
            }
        }
        return handlers;
    }
}

