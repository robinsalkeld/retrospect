package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ThreadMirrorDeathRequest;

public class WrappingThreadMirrorDeathRequest extends WrappingMirrorEventRequest implements ThreadMirrorDeathRequest {

    public WrappingThreadMirrorDeathRequest(WrappingVirtualMachine vm, ThreadMirrorDeathRequest wrapped) {
        super(vm, wrapped);
    }
}
