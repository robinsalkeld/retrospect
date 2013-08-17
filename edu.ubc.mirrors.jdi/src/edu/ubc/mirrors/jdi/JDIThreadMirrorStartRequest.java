package edu.ubc.mirrors.jdi;

import com.sun.jdi.request.ThreadStartRequest;

import edu.ubc.mirrors.ThreadMirrorStartRequest;

public class JDIThreadMirrorStartRequest extends JDIEventRequest implements ThreadMirrorStartRequest {

    public JDIThreadMirrorStartRequest(JDIVirtualMachineMirror vm, ThreadStartRequest wrapped) {
        super(vm, wrapped);
    }

    @Override
    public void addClassFilter(String classNamePattern) {
        throw new UnsupportedOperationException();
    }
}
