package edu.ubc.mirrors.asjdi;

import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;

import edu.ubc.mirrors.MirrorEventQueue;
import edu.ubc.mirrors.asjdi.MirrorsEventSet;
import edu.ubc.mirrors.asjdi.MirrorsMirror;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsEventQueue extends MirrorsMirror implements EventQueue {

    public MirrorsEventQueue(MirrorsVirtualMachine vm, MirrorEventQueue wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    private final MirrorEventQueue wrapped;
    
    @Override
    public EventSet remove() throws InterruptedException {
        return new MirrorsEventSet(vm, wrapped.remove());
    }

    @Override
    public EventSet remove(long arg0) throws InterruptedException {
        return new MirrorsEventSet(vm, wrapped.remove());
    }

}
