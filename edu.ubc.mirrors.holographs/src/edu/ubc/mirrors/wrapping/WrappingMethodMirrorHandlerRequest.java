package edu.ubc.mirrors.wrapping;

import java.util.List;

import edu.ubc.mirrors.MethodMirrorHandlerRequest;

public class WrappingMethodMirrorHandlerRequest extends WrappingMirrorEventRequest implements MethodMirrorHandlerRequest {

    private final MethodMirrorHandlerRequest wrapped;
    
    public WrappingMethodMirrorHandlerRequest(WrappingVirtualMachine vm, MethodMirrorHandlerRequest wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    @Override
    public void setMethodFilter(String declaringClass, String name, List<String> parameterTypeNames) {
        wrapped.setMethodFilter(declaringClass, name, parameterTypeNames);
    }
}
