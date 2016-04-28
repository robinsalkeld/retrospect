package edu.ubc.mirrors.jdi;

import com.sun.jdi.request.VMDeathRequest;

import edu.ubc.mirrors.VMMirrorDeathRequest;

public class JDIVMMirrorDeathRequest extends JDIEventRequest implements VMMirrorDeathRequest {

    public JDIVMMirrorDeathRequest(JDIVirtualMachineMirror vm, VMDeathRequest wrapped) {
        super(vm, wrapped);
    }

    @Override
    public void addClassFilter(String classNamePattern) {
        throw new UnsupportedOperationException();
    }
}
