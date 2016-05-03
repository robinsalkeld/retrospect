package edu.ubc.mirrors.tod;

import java.util.Collections;
import java.util.List;

import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VMMirrorDeathEvent;

public class TODVMMirrorDeathEvent extends TODMirrorEvent implements VMMirrorDeathEvent {

    public TODVMMirrorDeathEvent(TODVirtualMachineMirror vm, TODMirrorEventRequest request, VMDeathEvent event) {
        super(vm, request, event);
    }

    @Override
    public List<Object> arguments() {
        return Collections.emptyList();
    }

    @Override
    public ThreadMirror thread() {
        return null;
    }

    @Override
    public MirrorInvocationHandler getProceed() {
        return MirrorInvocationHandler.NONE;
    }

    @Override
    public void setProceed(MirrorInvocationHandler handler) {
        throw new UnsupportedOperationException();
    }
}
