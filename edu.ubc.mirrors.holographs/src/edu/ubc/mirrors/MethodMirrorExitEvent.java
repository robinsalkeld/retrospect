package edu.ubc.mirrors;

public interface MethodMirrorExitEvent extends MirrorEvent {

    public ThreadMirror thread();
    public MethodMirror method();
}
