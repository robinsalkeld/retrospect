package edu.ubc.mirrors.tod;

import tod.core.database.event.IBehaviorCallEvent;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorEntryEvent;

public class TODConstructorMirrorEntryEvent extends TODMirrorEvent implements ConstructorMirrorEntryEvent {

    public TODConstructorMirrorEntryEvent(TODVirtualMachineMirror vm, TODMirrorEventRequest request, IBehaviorCallEvent logEvent) {
        super(vm, request, logEvent);
        this.logEvent = logEvent;
    }
    
    private final IBehaviorCallEvent logEvent;
    
    @Override
    public ConstructorMirror constructor() {
        return vm.makeConstructorMirror(logEvent.getExecutedBehavior());
    }
}
