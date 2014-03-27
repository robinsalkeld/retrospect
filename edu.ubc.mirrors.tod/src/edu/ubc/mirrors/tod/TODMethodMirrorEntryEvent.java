package edu.ubc.mirrors.tod;

import tod.core.database.event.IBehaviorCallEvent;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryEvent;

public class TODMethodMirrorEntryEvent extends TODMirrorEvent implements MethodMirrorEntryEvent {

    public TODMethodMirrorEntryEvent(TODVirtualMachineMirror vm, TODMirrorEventRequest request, IBehaviorCallEvent logEvent) {
        super(vm, request, logEvent);
        this.logEvent = logEvent;
    }
    
    private final IBehaviorCallEvent logEvent;
    
    @Override
    public MethodMirror method() {
        return vm.makeMethodMirror(logEvent.getExecutedBehavior());
    }
}
