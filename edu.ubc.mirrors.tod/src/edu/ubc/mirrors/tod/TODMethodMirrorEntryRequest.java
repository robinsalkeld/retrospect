package edu.ubc.mirrors.tod;

import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.BehaviorKind;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryRequest;

public class TODMethodMirrorEntryRequest extends TODBehaviorEventRequest implements MethodMirrorEntryRequest {

    public TODMethodMirrorEntryRequest(TODVirtualMachineMirror vm) {
        super(vm);
    }

    @Override
    protected boolean matchConstructors() {
        return false;
    }
    
    @Override
    protected boolean matchExits() {
        return false;
    }
    
    @Override
    public void setMethodFilter(MethodMirror method) {
        this.behaviorInfo = ((TODMethodOrConstructorMirror)method).behaviourInfo;
    }
}
