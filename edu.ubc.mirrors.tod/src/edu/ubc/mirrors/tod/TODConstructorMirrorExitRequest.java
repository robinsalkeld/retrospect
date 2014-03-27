package edu.ubc.mirrors.tod;

import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.BehaviorKind;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorExitRequest;

public class TODConstructorMirrorExitRequest extends TODBehaviorEventRequest implements ConstructorMirrorExitRequest {

    public TODConstructorMirrorExitRequest(TODVirtualMachineMirror vm) {
        super(vm);
    }
    
    @Override
    protected boolean matchConstructors() {
        return true;
    }
    
    @Override
    protected boolean matchExits() {
        return true;
    }
    
    @Override
    public void setConstructorFilter(ConstructorMirror constructor) {
        this.behaviorInfo = ((TODMethodOrConstructorMirror)constructor).behaviourInfo;
    }
}
