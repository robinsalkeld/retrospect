package edu.ubc.mirrors.tod;

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
    public MethodMirror method() {
        return vm.makeMethodMirror(logEvent.getOperationBehavior());
    }
}
