package edu.ubc.mirrors.holographs.jdi;

import com.sun.jdi.Location;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.BreakpointEvent;

public class HolographBreakpointEvent extends HolographEvent implements BreakpointEvent {

    private final BreakpointEvent wrapped;
    
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
     * @see com.sun.jdi.event.LocatableEvent#thread()
     */
    public ThreadReference thread() {
        return vm.wrapThread(wrapped.thread());
    }

    protected HolographBreakpointEvent(JDIHolographVirtualMachine vm,
            BreakpointEvent wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

}
