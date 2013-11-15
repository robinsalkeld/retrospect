package edu.ubc.mirrors.jdi;

import com.sun.jdi.event.BreakpointEvent;

import edu.ubc.mirrors.MirrorLocation;
import edu.ubc.mirrors.MirrorLocationEvent;
import edu.ubc.mirrors.ThreadMirror;

public class JDIMirrorLocationEvent extends JDIMirrorEvent implements MirrorLocationEvent {

    private final BreakpointEvent wrapped;
    
    public JDIMirrorLocationEvent(JDIVirtualMachineMirror vm, BreakpointEvent wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }
    
    @Override
    public MirrorLocation location() {
        return vm.makeMirrorLocation(wrapped.location());
    }
    
    @Override
    public ThreadMirror thread() {
        return (ThreadMirror)vm.makeMirror(wrapped.thread());
    }
}
