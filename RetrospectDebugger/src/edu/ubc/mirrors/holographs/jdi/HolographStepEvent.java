package edu.ubc.mirrors.holographs.jdi;

import com.sun.jdi.Location;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.request.EventRequest;

public class HolographStepEvent extends HolographEvent implements StepEvent {

    protected HolographStepEvent(JDIHolographVirtualMachine vm, StepEvent wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    private final StepEvent wrapped;

    /**
     * @return
     * @see com.sun.jdi.Mirror#toString()
     */
    public String toString() {
        return wrapped.toString();
    }

    /**
     * @return
     * @see com.sun.jdi.Locatable#location()
     */
    public Location location() {
        return vm.wrapLocation(wrapped.location());
    }

    /**
     * @return
     * @see com.sun.jdi.event.Event#request()
     */
    public EventRequest request() {
        return wrapped.request();
    }

    /**
     * @return
     * @see com.sun.jdi.event.LocatableEvent#thread()
     */
    public ThreadReference thread() {
        return vm.wrapThread(wrapped.thread());
    }

    
}
