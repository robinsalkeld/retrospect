package edu.ubc.mirrors.holographs.jdi;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.request.EventRequest;

public class HolographClassPrepareEvent extends HolographEvent implements ClassPrepareEvent {

    private final ClassPrepareEvent wrapped;

    public HolographClassPrepareEvent(JDIHolographVirtualMachine vm, ClassPrepareEvent wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }
    
    /**
     * @return
     * @see com.sun.jdi.event.ClassPrepareEvent#referenceType()
     */
    public ReferenceType referenceType() {
        return vm.wrapReferenceType(wrapped.referenceType());
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
     * @see com.sun.jdi.event.ClassPrepareEvent#thread()
     */
    public ThreadReference thread() {
        return vm.wrapThread(wrapped.thread());
    }

    /**
     * @return
     * @see com.sun.jdi.Mirror#toString()
     */
    public String toString() {
        return wrapped.toString();
    }
}
