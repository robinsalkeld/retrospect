package edu.ubc.mirrors.jdi;

import com.sun.jdi.event.VMDeathEvent;

import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VMMirrorDeathEvent;

public class JDIVMMirrorDeathEvent extends JDIMirrorEvent implements VMMirrorDeathEvent {

    public JDIVMMirrorDeathEvent(JDIVirtualMachineMirror vm, VMDeathEvent wrapped) {
        super(vm, wrapped);
    }

    @Override
    public ThreadMirror thread() {
        return null;
    }

}
