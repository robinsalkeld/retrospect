package edu.ubc.mirrors.tod;

import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorExitRequest;

public class TODMethodMirrorExitRequest extends TODBehaviorEventRequest implements MethodMirrorExitRequest {

    public TODMethodMirrorExitRequest(TODVirtualMachineMirror vm) {
        super(vm);
    }
    
    @Override
    protected boolean matchConstructors() {
        return false;
    }
    
    @Override
    protected boolean matchExits() {
        return true;
    }
    
    @Override
    public void setMethodFilter(MethodMirror method) {
        this.behaviorInfo = ((TODMethodOrConstructorMirror)method).behaviourInfo;
    }
}
