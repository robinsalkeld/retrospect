package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ThreadMirrorStartRequest;

public class WrappingThreadMirrorStartRequest extends WrappingMirrorEventRequest implements ThreadMirrorStartRequest {

    public WrappingThreadMirrorStartRequest(WrappingVirtualMachine vm, ThreadMirrorStartRequest wrapped) {
        super(vm, wrapped);
    }
}