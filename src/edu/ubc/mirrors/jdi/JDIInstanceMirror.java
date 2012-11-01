package edu.ubc.mirrors.jdi;

import com.sun.jdi.ObjectReference;

import edu.ubc.mirrors.InstanceMirror;

public class JDIInstanceMirror extends JDIObjectMirror implements InstanceMirror {

    public JDIInstanceMirror(JDIVirtualMachineMirror vm, ObjectReference t) {
        super(vm, t);
    }

}
