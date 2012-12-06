package edu.ubc.mirrors.jdi;

import com.sun.jdi.request.ThreadDeathRequest;

import edu.ubc.mirrors.ThreadMirrorDeathRequest;

public class JDIThreadMirrorDeathRequest extends JDIEventRequest implements ThreadMirrorDeathRequest {

    private final ThreadDeathRequest wrapped;
    
    public JDIThreadMirrorDeathRequest(JDIVirtualMachineMirror vm, ThreadDeathRequest wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    @Override
    public void addClassFilter(String classNamePattern) {
        throw new UnsupportedOperationException();
    }
}
