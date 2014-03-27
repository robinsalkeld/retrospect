package edu.ubc.mirrors.tod;

import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorEntryRequest;

public class TODConstructorMirrorEntryRequest extends TODBehaviorEventRequest implements ConstructorMirrorEntryRequest {

    public TODConstructorMirrorEntryRequest(TODVirtualMachineMirror vm) {
        super(vm);
    }

    @Override
    protected boolean matchConstructors() {
        return true;
    }
    
    @Override
    protected boolean matchExits() {
        return false;
    }
    
    @Override
    public void setConstructorFilter(ConstructorMirror constructor) {
        this.behaviorInfo = ((TODMethodOrConstructorMirror)constructor).behaviourInfo;
    }
}
