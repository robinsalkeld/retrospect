package edu.ubc.mirrors.asjdi;

import com.sun.jdi.event.Event;
import com.sun.jdi.request.EventRequest;

import edu.ubc.mirrors.MirrorEvent;

public class MirrorsEvent extends MirrorsMirror implements Event {

    private final MirrorEvent wrapped;
    
    public MirrorsEvent(MirrorsVirtualMachine vm, MirrorEvent wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    @Override
    public EventRequest request() {
        return (EventRequest)wrapped.request().getProperty(MirrorsEventRequest.WRAPPER);
    }
}
