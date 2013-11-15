package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.MirrorLocation;
import edu.ubc.mirrors.MirrorLocationEvent;
import edu.ubc.mirrors.ThreadMirror;

public class WrappingMirrorLocationEvent extends WrappingMirrorEvent implements MirrorLocationEvent {

    private final MirrorLocationEvent wrapped;
    
    public WrappingMirrorLocationEvent(WrappingVirtualMachine vm, MirrorLocationEvent wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }
    
    @Override
    public MirrorLocation location() {
        return vm.wrapLocation(wrapped.location());
    }
    
    @Override
    public ThreadMirror thread() {
        return (ThreadMirror)vm.getWrappedMirror(wrapped.thread());
    }
}
