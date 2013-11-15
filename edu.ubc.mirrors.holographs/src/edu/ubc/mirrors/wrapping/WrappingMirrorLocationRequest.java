package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.MirrorLocationRequest;

public class WrappingMirrorLocationRequest extends WrappingMirrorEventRequest implements MirrorLocationRequest {

    public WrappingMirrorLocationRequest(WrappingVirtualMachine vm, MirrorLocationRequest wrapped) {
        super(vm, wrapped);
    }
}
