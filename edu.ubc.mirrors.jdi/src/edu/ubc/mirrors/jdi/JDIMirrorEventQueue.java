package edu.ubc.mirrors.jdi;

import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.VMDeathEvent;

import edu.ubc.mirrors.MirrorEventQueue;
import edu.ubc.mirrors.MirrorEventSet;

public class JDIMirrorEventQueue extends JDIMirror implements MirrorEventQueue {

    private final EventQueue wrapped;

    public JDIMirrorEventQueue(JDIVirtualMachineMirror vm, EventQueue wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public MirrorEventSet remove() throws InterruptedException {
	EventSet eventSet = wrapped.remove();
	if (eventSet.size() == 1 && eventSet.iterator().next() instanceof VMDeathEvent) {
	    return null;
	} else {
	    return new JDIMirrorEventSet(vm, eventSet);
	}
    }
    
    
}
