package edu.ubc.mirrors.holographs.jdi;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.request.EventRequest;

public class HolographThreadStartEvent extends HolographEvent implements ThreadStartEvent {

    private final ThreadStartEvent wrapped;

    /**
     * @return
     * @see com.sun.jdi.Mirror#toString()
     */
    public String toString() {
        return wrapped.toString();
    }

    /**
     * @return
     * @see com.sun.jdi.event.ThreadStartEvent#thread()
     */
    public ThreadReference thread() {
        return vm.wrapThread(wrapped.thread());
    }

    /**
     * @return
     * @see com.sun.jdi.event.Event#request()
     */
    public EventRequest request() {
        return wrapped.request();
    }

    public HolographThreadStartEvent(JDIHolographVirtualMachine vm, ThreadStartEvent wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }
    
    
}
