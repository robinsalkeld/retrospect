package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.VMMirrorDeathEvent;

public class WrappingVMMirrorDeathEvent extends WrappingMirrorEvent implements VMMirrorDeathEvent {

    public WrappingVMMirrorDeathEvent(WrappingVirtualMachine vm, MirrorEvent wrapped) {
        super(vm, wrapped);
    }

}
