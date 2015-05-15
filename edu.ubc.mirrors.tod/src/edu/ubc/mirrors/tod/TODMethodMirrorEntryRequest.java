package edu.ubc.mirrors.tod;

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
