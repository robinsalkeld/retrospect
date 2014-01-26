package edu.ubc.mirrors.tod;

import tod.core.database.event.IBehaviorCallEvent;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryEvent;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.ThreadMirror;

public class TODMethodMirrorEntryEvent extends TODMirrorEvent implements MethodMirrorEntryEvent {

    public TODMethodMirrorEntryEvent(TODVirtualMachineMirror vm, TODMirrorEventRequest request, IBehaviorCallEvent logEvent) {
        super(vm, request);
        this.logEvent = logEvent;
    }
    
    private final IBehaviorCallEvent logEvent;
    
    @Override
    public ThreadMirror thread() {
        return vm.makeThreadMirror(logEvent.getThread());
    }

    @Override
    public MethodMirror method() {
        return new TODMethodOrConstructorMirror(vm, logEvent.getExecutedBehavior());
    }
}
