package edu.ubc.mirrors.tod;

import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequest;

public class TODMirrorEvent implements MirrorEvent {

    protected final TODVirtualMachineMirror vm;
    private final MirrorEventRequest request;
    
    public TODMirrorEvent(TODVirtualMachineMirror vm, MirrorEventRequest request) {
        this.vm = vm;
        this.request = request;
    }
    
    @Override
    public MirrorEventRequest request() {
        return request;
    }
}
