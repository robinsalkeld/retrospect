package edu.ubc.mirrors;

public interface MirrorEventQueue {

    public MirrorEventSet remove() throws InterruptedException;
}
