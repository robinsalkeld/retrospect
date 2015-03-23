package edu.ubc.mirrors.holographs;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventQueue;
import edu.ubc.mirrors.MirrorEventSet;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.wrapping.WrappingMirrorEventQueue;

public class HolographEventQueue extends WrappingMirrorEventQueue {

    private final VirtualMachineHolograph vm;
    
    // Used as a timestamp for invalidating caches
    private long events = 0;
    
    private class HolographEventSet extends AbstractSet<MirrorEvent> implements MirrorEventSet {

        private final ThreadMirror thread;
        private final Set<MirrorEvent> events = new HashSet<MirrorEvent>();
        
        public HolographEventSet(ThreadMirror thread) {
            this.thread = thread;
        }

        @Override
        public void resume() {
            HolographEventQueue.this.resume();
        }

        @Override
        public ThreadMirror thread() {
            return thread;
        }

        @Override
        public Iterator<MirrorEvent> iterator() {
            return events.iterator();
        }

        @Override
        public int size() {
            return events.size();
        }
        
    }
    
    private MirrorEventSet eventSet = null;
    
    public HolographEventQueue(VirtualMachineHolograph vm, MirrorEventQueue wrapped) {
    	super(vm, wrapped);
    	this.vm = vm;
    }

    protected synchronized void raiseEvent(MirrorEvent event) {
        eventSet = new HolographEventSet(event.thread());
        eventSet.add(event);
        try {
            wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected void resume() {
        eventSet = null;
        notify();
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
