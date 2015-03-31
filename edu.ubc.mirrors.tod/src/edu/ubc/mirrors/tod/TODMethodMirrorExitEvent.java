package edu.ubc.mirrors.tod;

import java.util.Collections;
import java.util.List;

import tod.core.database.event.IBehaviorExitEvent;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorExitEvent;

public class TODMethodMirrorExitEvent extends TODMirrorEvent implements MethodMirrorExitEvent {

    public TODMethodMirrorExitEvent(TODVirtualMachineMirror vm, TODMirrorEventRequest request, IBehaviorExitEvent logEvent) {
        super(vm, request, logEvent);
        this.logEvent = logEvent;
    }
    
    private final IBehaviorExitEvent logEvent;
    
    @Override
    public List<Object> arguments() {
        return Collections.emptyList();
    }
    
    @Override
    public MethodMirror method() {
        return vm.makeMethodMirror(logEvent.getOperationBehavior());
    }
    
    @Override
    public Object returnValue() {
        return vm.wrapValue(method().getReturnType(), logEvent.getResult());
    }
}
