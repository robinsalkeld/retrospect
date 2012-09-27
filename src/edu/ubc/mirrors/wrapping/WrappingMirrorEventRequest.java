package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.MethodMirrorEntryRequest;
import edu.ubc.mirrors.MirrorEventRequest;

public class WrappingMirrorEventRequest implements MirrorEventRequest {
    protected final WrappingVirtualMachine vm;
    private final MethodMirrorEntryRequest wrapped;

    public WrappingMirrorEventRequest(WrappingVirtualMachine vm, MethodMirrorEntryRequest wrapped) {
	this.vm = vm;
	this.wrapped = wrapped;
    }

    @Override
    public void enable() {
	wrapped.enable();
    }

    @Override
    public void disable() {
	wrapped.disable();
    }

    @Override
    public void setEnabled(boolean enabled) {
	wrapped.setEnabled(enabled);
    }
}
