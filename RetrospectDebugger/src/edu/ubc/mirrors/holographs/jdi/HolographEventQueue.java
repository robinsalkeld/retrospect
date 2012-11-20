package edu.ubc.mirrors.holographs.jdi;

import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;

public class HolographEventQueue extends Holograph implements EventQueue {

    private final EventQueue wrapped;

    public HolographEventQueue(JDIHolographVirtualMachine vm, EventQueue wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }
    
    /**
     * @return
     * @throws InterruptedException
     * @see com.sun.jdi.event.EventQueue#remove()
     */
    public EventSet remove() throws InterruptedException {
        return vm.wrapEventSet(wrapped.remove());
    }

    /**
     * @param arg0
     * @return
     * @throws InterruptedException
     * @see com.sun.jdi.event.EventQueue#remove(long)
     */
    public EventSet remove(long arg0) throws InterruptedException {
        return vm.wrapEventSet(wrapped.remove(arg0));
    }

    /**
     * @return
     * @see com.sun.jdi.Mirror#toString()
     */
    public String toString() {
        return wrapped.toString();
    }
}
