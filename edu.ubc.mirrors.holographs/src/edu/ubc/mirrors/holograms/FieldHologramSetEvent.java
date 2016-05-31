package edu.ubc.mirrors.holograms;

import java.util.Arrays;
import java.util.List;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FieldMirrorSetHandlerRequest;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.ThreadMirror;

public class FieldHologramSetEvent implements MirrorEvent {

    private final FieldMirrorSetHandlerRequest request;
    private final ThreadMirror thread;
    private final InstanceMirror target;
    protected final FieldMirror field;
    private final Object newValue;
    private MirrorInvocationHandler proceed;
    
    public FieldHologramSetEvent(FieldMirrorSetHandlerRequest request, ThreadMirror thread, InstanceMirror target, FieldMirror field, Object newValue) {
        this.request = request;
        this.thread = thread;
        this.target = target;
        this.field = field;
        this.newValue = newValue;
    }

    @Override
    public MirrorEventRequest request() {
        return request;
    }

    @Override
    public ThreadMirror thread() {
        return thread;
    }

    @Override
    public List<Object> arguments() {
        return Arrays.asList(target, newValue);
    }

    @Override
    public MirrorInvocationHandler getProceed() {
        return proceed;
    }

    @Override
    public void setProceed(MirrorInvocationHandler proceed) {
        this.proceed = proceed;
    }
}
