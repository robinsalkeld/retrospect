package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.MirrorEventQueue;
import edu.ubc.mirrors.MirrorEventSet;

public class WrappingMirrorEventQueue implements MirrorEventQueue {

    private final WrappingVirtualMachine vm;
    private final MirrorEventQueue wrapped;
    
    public WrappingMirrorEventQueue(WrappingVirtualMachine vm, MirrorEventQueue wrapped) {
	this.vm = vm;
	this.wrapped = wrapped;
    }

    @Override
    public MirrorEventSet remove() throws InterruptedException {
	MirrorEventSet eventSet = wrapped.remove();
	return eventSet == null ? null : new WrappingMirrorEventSet(vm, eventSet);
    }

}