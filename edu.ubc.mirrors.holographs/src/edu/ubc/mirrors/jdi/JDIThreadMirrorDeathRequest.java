package edu.ubc.mirrors.jdi;

import com.sun.jdi.request.ThreadDeathRequest;

import edu.ubc.mirrors.ThreadMirrorDeathRequest;

public class JDIThreadMirrorDeathRequest extends JDIEventRequest implements ThreadMirrorDeathRequest {

    public JDIThreadMirrorDeathRequest(JDIVirtualMachineMirror vm, ThreadDeathRequest wrapped) {
        super(vm, wrapped);
    }

    @Override
    public void addClassFilter(String classNamePattern) {
        throw new UnsupportedOperationException();
    }
}
