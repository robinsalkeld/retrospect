package edu.ubc.mirrors.holograms;

import java.util.HashSet;
import java.util.Set;

import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequest;

public abstract class HologramEvent implements MirrorEvent {

    private final Set<MirrorEventRequest> requests;
    
    public HologramEvent(MirrorEventRequest request) {
        requests = new HashSet<MirrorEventRequest>();
        requests.add(request);
    }

    @Override
    public Set<MirrorEventRequest> requests() {
        return requests;
    }
}
