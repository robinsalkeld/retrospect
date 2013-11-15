package edu.ubc.mirrors;

public interface MirrorLocationEvent extends MirrorEvent {

    public MirrorLocation location();
    public ThreadMirror thread();
}
