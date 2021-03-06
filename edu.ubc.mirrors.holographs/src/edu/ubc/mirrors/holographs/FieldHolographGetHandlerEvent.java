package edu.ubc.mirrors.holographs;

import java.util.Collections;
import java.util.List;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FieldMirrorGetHandlerEvent;
import edu.ubc.mirrors.FieldMirrorGetHandlerRequest;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.ThreadMirror;

public class FieldHolographGetHandlerEvent extends HolographEvent implements FieldMirrorGetHandlerEvent {

    private final ThreadMirror thread;
    private final InstanceMirror target;
    private final FieldMirror field;
    private MirrorInvocationHandler proceed;
    
    public FieldHolographGetHandlerEvent(FieldMirrorGetHandlerRequest request, ThreadMirror thread,
            InstanceMirror target, FieldMirror field, MirrorInvocationHandler proceed) {
        super(request);
        this.thread = thread;
        this.target = target;
        this.field = field;
        this.proceed = proceed;
    }

    @Override
    public ThreadMirror thread() {
        return thread;
    }

    @Override
    public List<Object> arguments() {
        return Collections.<Object>singletonList(target);
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
    public String toString() {
        return getClass().getSimpleName() + " on " + field;
    }
}
