package edu.ubc.mirrors;

public interface MethodMirrorEntryEvent extends MirrorEvent {

    public ThreadMirror thread();
    public MethodMirror method();
}
