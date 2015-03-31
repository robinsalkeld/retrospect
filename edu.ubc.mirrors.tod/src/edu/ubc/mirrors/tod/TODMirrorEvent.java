package edu.ubc.mirrors.tod;

import tod.core.database.event.ILogEvent;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.ThreadMirror;

public abstract class TODMirrorEvent implements MirrorEvent {

    protected final TODVirtualMachineMirror vm;
    private final MirrorEventRequest request;
    private final ILogEvent event;
    
    public TODMirrorEvent(TODVirtualMachineMirror vm, MirrorEventRequest request, ILogEvent event) {
        this.vm = vm;
        this.request = request;
        this.event = event;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TODMirrorEvent)) {
            return false;
        }
        
        TODMirrorEvent other = (TODMirrorEvent)obj;
        return vm.equals(other.vm) && event.equals(other.event);
    }
    
    @Override
    public int hashCode() {
        return 37 * vm.hashCode() * vm.hashCode();
    }
    
    public ThreadMirror thread() {
        return vm.makeThreadMirror(event.getThread());
    }
    
    @Override
    public MirrorEventRequest request() {
        return request;
    }
    
    @Override
    public MirrorInvocationHandler getProceed() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void setProceed(MirrorInvocationHandler handler) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + event;
    }
}
