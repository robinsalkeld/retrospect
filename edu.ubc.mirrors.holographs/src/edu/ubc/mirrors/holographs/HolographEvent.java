package edu.ubc.mirrors.holographs;

import java.util.HashSet;
import java.util.Set;

import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequest;

public abstract class HolographEvent implements MirrorEvent {

    private final Set<MirrorEventRequest> requests;
    
    public HolographEvent(MirrorEventRequest request) {
        requests = new HashSet<MirrorEventRequest>();
        requests.add(request);
    }

    @Override
    public Set<MirrorEventRequest> requests() {
        return requests;
    }
}
