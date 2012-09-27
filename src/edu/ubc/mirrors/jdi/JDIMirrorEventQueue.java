package edu.ubc.mirrors.jdi;

import com.sun.jdi.event.EventQueue;

import edu.ubc.mirrors.MirrorEventQueue;
import edu.ubc.mirrors.MirrorEventSet;

public class JDIMirrorEventQueue implements MirrorEventQueue {

    private final EventQueue wrapped;

    public JDIMirrorEventQueue(EventQueue wrapped) {
	this.wrapped = wrapped;
    }

    @Override
    public MirrorEventSet remove() throws InterruptedException {
	return new JDIMirrorEventSet(wrapped.remove());
    }
    
    
}
