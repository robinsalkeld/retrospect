package edu.ubc.mirrors.tod;

import tod.core.database.event.IBehaviorExitEvent;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorExitEvent;

public class TODConstructorMirrorExitEvent extends TODMirrorEvent implements ConstructorMirrorExitEvent {

    public TODConstructorMirrorExitEvent(TODVirtualMachineMirror vm, TODMirrorEventRequest request, IBehaviorExitEvent logEvent) {
        super(vm, request, logEvent);
        this.logEvent = logEvent;
    }
    
    private final IBehaviorExitEvent logEvent;
    
    @Override
    public ConstructorMirror constructor() {
        return vm.makeConstructorMirror(logEvent.getOperationBehavior());
    }
}
