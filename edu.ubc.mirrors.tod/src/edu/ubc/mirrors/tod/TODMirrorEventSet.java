package edu.ubc.mirrors.tod;

import java.util.HashSet;

import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.BehaviorKind;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventSet;

public class TODMirrorEventSet extends HashSet<MirrorEvent> implements MirrorEventSet {

    private static final long serialVersionUID = 8455197622920137674L;

    public TODMirrorEventSet() {
    }
    
    @Override
    public void resume() {
        // Nothing to do
    }
    
}
