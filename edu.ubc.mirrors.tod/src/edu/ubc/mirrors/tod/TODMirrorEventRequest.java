package edu.ubc.mirrors.tod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.BehaviorKind;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequest;

public class TODMirrorEventRequest implements MirrorEventRequest, Iterator<MirrorEvent>, Comparable<TODMirrorEventRequest> {

    private final TODVirtualMachineMirror vm;
    private boolean enabled = false;
    private Map<Object, Object> properties = new HashMap<Object, Object>();
    private IEventBrowser eventBrowser;
    private ILogEvent nextEvent;
    
    public TODMirrorEventRequest(TODVirtualMachineMirror vm) {
        this.vm = vm;
    }
    
    @Override
    public void enable() {
        setEnabled(true);
    }

    @Override
    public void disable() {
        setEnabled(false);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Object getProperty(Object key) {
        return properties.get(key);
    }

    @Override
    public void putProperty(Object key, Object value) {
        properties.put(key, value);
    }

    @Override
    public void addClassFilter(String classNamePattern) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int compareTo(TODMirrorEventRequest o) {
        return Long.compare(getNextTimestamp(), o.getNextTimestamp());
    }

    public boolean hasNext() {
        if (nextEvent == null && eventBrowser.hasNext()) {
            nextEvent = eventBrowser.next();
        }
        return nextEvent != null;
    }
    
    public long getNextTimestamp() {
        if (nextEvent == null && eventBrowser.hasNext()) {
            nextEvent = eventBrowser.next();
        }
        
        return nextEvent != null ? nextEvent.getTimestamp() : Long.MAX_VALUE;
    }
    
    public MirrorEvent next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        
        MirrorEvent result = wrapEvent(nextEvent);
        nextEvent = null;
        return result;
    }
    
    public MirrorEvent wrapEvent(ILogEvent event) {
        if (event instanceof IBehaviorCallEvent) {
            IBehaviorCallEvent bce = (IBehaviorCallEvent)event;
            BehaviorKind kind = bce.getExecutedBehavior().getBehaviourKind();
            if (kind != BehaviorKind.STATIC_INIT) {
                return new TODMethodMirrorEntryEvent(vm, this, bce);
            } else {
                return null;
            }
        } else if (event instanceof IBehaviorExitEvent) {
            return null;
        } else {
            return null;
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
