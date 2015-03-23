package edu.ubc.mirrors;

import java.util.List;

public interface ConstructorMirrorHandlerEvent extends MirrorEvent {

    public ConstructorMirror constructor();
    public List<Object> arguments();
    public MirrorInvocationHandler proceed();
}
