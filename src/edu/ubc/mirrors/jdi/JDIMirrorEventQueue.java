package edu.ubc.mirrors.jdi;

import com.sun.jdi.event.EventQueue;

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
	return new JDIMirrorEventSet(vm, wrapped.remove());
    }
    
    
}
