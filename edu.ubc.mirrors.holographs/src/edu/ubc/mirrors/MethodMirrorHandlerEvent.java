package edu.ubc.mirrors;

import java.util.List;

public interface MethodMirrorHandlerEvent extends InvocableMirrorEvent {

    public MethodMirror method();
    public List<Object> arguments();
}
