package edu.ubc.mirrors.tod;

import tod.core.database.event.ExternalPointer;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;

public class VMDeathEvent implements ILogEvent {

    private final IThreadInfo thread;
    private final long timestamp;
    
    public VMDeathEvent(IThreadInfo thread, long timestamp) {
        this.thread = thread;
        this.timestamp = timestamp;
    }
    
    @Override
    public ExternalPointer getPointer() {
        return null;
    }
    
    @Override
    public IHostInfo getHost() {
        return null;
    }

    @Override
    public IThreadInfo getThread() {
        return thread;
    }

    @Override
    public int getDepth() {
        return 0;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public ExternalPointer getParentPointer() {
        return null;
    }

    @Override
    public IBehaviorCallEvent getParent() {
        return null;
    }

    @Override
    public int[] getAdviceCFlow() {
        return null;
    }

}
