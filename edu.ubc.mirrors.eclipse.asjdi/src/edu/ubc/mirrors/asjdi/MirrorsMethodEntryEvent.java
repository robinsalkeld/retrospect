package edu.ubc.mirrors.asjdi;

import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.MethodEntryEvent;

import edu.ubc.mirrors.MethodMirrorEntryEvent;
import edu.ubc.mirrors.asjdi.MirrorsMethod;
import edu.ubc.mirrors.asjdi.MirrorsEvent;
import edu.ubc.mirrors.asjdi.MirrorsLocation;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsMethodEntryEvent extends MirrorsEvent implements MethodEntryEvent {

    private final MethodMirrorEntryEvent wrapped;
    
    public MirrorsMethodEntryEvent(MirrorsVirtualMachine vm, MethodMirrorEntryEvent wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    @Override
    public ThreadReference thread() {
        return (ThreadReference)vm.wrapMirror(wrapped.thread());
    }

    @Override
    public Location location() {
        return MirrorsLocation.locationForMethod(vm, wrapped.method());
    }

    @Override
    public Method method() {
        return new MirrorsMethod(vm, wrapped.method());
    }

}
