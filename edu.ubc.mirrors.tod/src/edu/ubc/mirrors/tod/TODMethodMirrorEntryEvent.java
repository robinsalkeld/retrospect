package edu.ubc.mirrors.tod;

import java.util.ArrayList;
import java.util.List;

import tod.core.database.event.IBehaviorCallEvent;
import edu.ubc.mirrors.ClassMirror;
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
    
    @Override
    public List<Object> arguments() {
        List<Object> result = new ArrayList<Object>();
        List<ClassMirror> argTypes = method().getParameterTypes();
        Object[] logArgs = logEvent.getArguments();
        for (int i = 0; i < logArgs.length; i++) {
            result.add(vm.wrapValue(argTypes.get(i), logArgs[i]));
        }
        return result;
    }
    
    @Override
    public void skip(Object returnValue) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + method() + " @ " + logEvent.getTimestamp();
    }
}
