package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.VMMirrorDeathRequest;

public class WrappingVMMirrorDeathRequest extends WrappingMirrorEventRequest implements VMMirrorDeathRequest {

    public WrappingVMMirrorDeathRequest(WrappingVirtualMachine vm, MirrorEventRequest wrapped) {
        super(vm, wrapped);
    }
    
}
