package edu.ubc.mirrors;

import java.util.List;

public interface InvocableMirrorEvent extends MirrorEvent {

    public List<Object> arguments();
    
    public MirrorInvocationHandler getProceed();
    
    public InvocableMirrorEvent setProceed(MirrorInvocationHandler proceed, List<Object> arguments);
}
