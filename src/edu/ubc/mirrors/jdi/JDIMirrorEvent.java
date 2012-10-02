package edu.ubc.mirrors.jdi;

import com.sun.jdi.Mirror;

import edu.ubc.mirrors.MirrorEvent;

public class JDIMirrorEvent extends JDIMirror implements MirrorEvent {

    public JDIMirrorEvent(JDIVirtualMachineMirror vm, Mirror mirror) {
	super(vm, mirror);
    }

}
