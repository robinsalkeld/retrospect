package edu.ubc.mirrors.tod;

import java.util.Collections;
import java.util.List;

import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IBehaviorExitEvent;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorExitEvent;
import edu.ubc.mirrors.InstanceMirror;

public class TODConstructorMirrorExitEvent extends TODMirrorEvent implements ConstructorMirrorExitEvent {

    private final IBehaviorCallEvent callEvent;
    
    public TODConstructorMirrorExitEvent(TODVirtualMachineMirror vm, TODMirrorEventRequest request, IBehaviorExitEvent logEvent) {
        super(vm, request, logEvent);
        this.logEvent = logEvent;
        this.callEvent = vm.getEntryEvent(logEvent);
    }
    
    private final IBehaviorExitEvent logEvent;
    
    @Override
    public List<Object> arguments() {
        return Collections.emptyList();
    }
    
    @Override
    public ConstructorMirror constructor() {
        return vm.makeConstructorMirror(logEvent.getOperationBehavior());
    }
    
    @Override
    public InstanceMirror returnValue() {
        // logEvent.getResult() will be null
        return (InstanceMirror)vm.wrapValue(constructor().getDeclaringClass(), callEvent.getTarget());
    }
}
