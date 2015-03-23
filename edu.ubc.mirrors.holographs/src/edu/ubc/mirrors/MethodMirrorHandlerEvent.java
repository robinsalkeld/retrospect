package edu.ubc.mirrors;

import java.util.List;

public interface MethodMirrorHandlerEvent extends MirrorEvent {

    public MethodMirror method();
    public List<Object> arguments();
    public MirrorInvocationHandler proceed();
}
