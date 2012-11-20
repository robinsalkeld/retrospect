package edu.ubc.mirrors;

import java.util.Set;

public interface MirrorEventSet extends Set<MirrorEvent> {

    public void resume();
}
