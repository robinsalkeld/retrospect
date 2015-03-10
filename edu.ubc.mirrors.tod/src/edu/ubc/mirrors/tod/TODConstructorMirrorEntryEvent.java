package edu.ubc.mirrors.tod;

import java.util.ArrayList;
import java.util.List;

import tod.core.database.event.IBehaviorCallEvent;
import edu.ubc.mirrors.ClassMirror;
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
    
    @Override
    public List<Object> arguments() {
        List<Object> result = new ArrayList<Object>();
        List<ClassMirror> argTypes = constructor().getParameterTypes();
        Object[] logArgs = logEvent.getArguments();
        for (int i = 0; i < logArgs.length; i++) {
            result.add(vm.wrapValue(argTypes.get(i), logArgs[i]));
        }
        return result;
    }
}
