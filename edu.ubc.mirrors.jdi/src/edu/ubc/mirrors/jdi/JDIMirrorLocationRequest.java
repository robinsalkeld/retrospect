package edu.ubc.mirrors.jdi;

import com.sun.jdi.request.BreakpointRequest;

import edu.ubc.mirrors.MirrorLocationRequest;

public class JDIMirrorLocationRequest extends JDIEventRequest implements MirrorLocationRequest {

    private final BreakpointRequest wrapped;

    public JDIMirrorLocationRequest(JDIVirtualMachineMirror vm, BreakpointRequest wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    @Override
    public void addClassFilter(String classNamePattern) {
        throw new UnsupportedOperationException();
    }
}
