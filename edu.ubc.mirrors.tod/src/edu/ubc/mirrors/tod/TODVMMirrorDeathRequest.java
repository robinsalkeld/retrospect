package edu.ubc.mirrors.tod;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IThreadInfo;
import edu.ubc.mirrors.VMMirrorDeathRequest;

public class TODVMMirrorDeathRequest extends TODMirrorEventRequest implements VMMirrorDeathRequest {

    private final VMDeathEvent event;
    private boolean hasNext = true;
    
    public TODVMMirrorDeathRequest(TODVirtualMachineMirror vm) {
        super(vm);
        // TODO-RS: Hack so that we can invoke Shutdown.runHooks() a little later than it
        // would have actually ran
        IThreadInfo thread = vm.threadInfosByObjectId.values().iterator().next();
        event = new VMDeathEvent(thread);
    }

    @Override
    public void addClassFilter(String classNamePattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected IEventBrowser createEventBrowser() {
        return null;
    }

    @Override
    public boolean hasNext() {
        checkEnabled();
        return hasNext;
    }
    
    @Override
    public ILogEvent getNextEvent() {
        if (hasNext) {
            return event;
        } else {
            return null;
        }
    }
    
    @Override
    public TODMirrorEvent next() {
        checkEnabled();
        if (hasNext) {
            hasNext = false;
            return wrapEvent(event);
        } else {
            return null;
        }
    }
    
    @Override
    void setTimestamp(long t) {
        hasNext = true;
    }
}
