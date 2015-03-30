package edu.ubc.mirrors;

import java.util.List;

public interface ConstructorMirrorHandlerEvent extends InvocableMirrorEvent {

    public ConstructorMirror constructor();
    public List<Object> arguments();
}
