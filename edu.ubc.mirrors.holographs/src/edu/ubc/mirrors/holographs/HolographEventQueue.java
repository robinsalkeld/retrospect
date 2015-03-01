package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.MirrorEventQueue;
import edu.ubc.mirrors.MirrorEventSet;
import edu.ubc.mirrors.wrapping.WrappingMirrorEventQueue;

public class HolographEventQueue extends WrappingMirrorEventQueue {

    private final VirtualMachineHolograph vm;
    private long events = 0;
    
    public HolographEventQueue(VirtualMachineHolograph vm, MirrorEventQueue wrapped) {
    	super(vm, wrapped);
    	this.vm = vm;
    }

    @Override
    public MirrorEventSet remove() throws InterruptedException {
        MirrorEventSet result = super.remove();
        events++;
        return result;
    }
    
    public long getEventsCount() {
        return events;
    }
}
