package edu.ubc.mirrors.asjdi;

import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.event.MethodExitEvent;

import edu.ubc.mirrors.MethodMirrorExitEvent;
import edu.ubc.mirrors.asjdi.MethodMirrorMethod;
import edu.ubc.mirrors.asjdi.MirrorsEvent;
import edu.ubc.mirrors.asjdi.MirrorsLocation;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsMethodExitEvent extends MirrorsEvent implements MethodExitEvent {

    private final MethodMirrorExitEvent wrapped;
    
    public MirrorsMethodExitEvent(MirrorsVirtualMachine vm, MethodMirrorExitEvent wrapped) {
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
        return new MethodMirrorMethod(vm, wrapped.method());
    }

    @Override
    public Value returnValue() {
        throw new UnsupportedOperationException();
    }

}
