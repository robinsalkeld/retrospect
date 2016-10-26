package edu.ubc.mirrors.holographs;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FieldMirrorSetHandlerEvent;
import edu.ubc.mirrors.FieldMirrorSetHandlerRequest;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.ThreadMirror;

public class FieldHolographSetHandlerEvent extends HolographEvent implements FieldMirrorSetHandlerEvent {

    private final ThreadMirror thread;
    private final InstanceMirror target;
    private final FieldMirror field;
    private final Object newValue;
    private MirrorInvocationHandler proceed;
    
    public FieldHolographSetHandlerEvent(FieldMirrorSetHandlerRequest request, ThreadMirror thread,
            InstanceMirror target, FieldMirror field, Object newValue, MirrorInvocationHandler proceed) {
        super(request);
        this.thread = thread;
        this.target = target;
        this.field = field;
        this.newValue = newValue;
        this.proceed = proceed;
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
    public void setProceed(MirrorInvocationHandler handler) {
        this.proceed = handler;
    }

    @Override
    public InstanceMirror target() {
        return target;
    }

    @Override
    public FieldMirror field() {
        return field;
    }

    @Override
    public Object newValue() {
        return newValue;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + field;
    }
}
