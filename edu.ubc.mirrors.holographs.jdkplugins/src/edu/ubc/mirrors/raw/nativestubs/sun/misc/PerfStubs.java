package edu.ubc.mirrors.raw.nativestubs.sun.misc;

import java.nio.ByteBuffer;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class PerfStubs extends NativeStubs {

    public PerfStubs(ClassHolograph klass) {
	super(klass);
    }

    @StubMethod
    public void registerNatives() {
	// No need to do anything
    }
    
    @StubMethod
    public InstanceMirror createLong(InstanceMirror perf, InstanceMirror name, int derp, int florp, long blong) {
	VirtualMachineHolograph vm = getVM();
	
	// Just create an arbitrary buffer - this VM doesn't expose any performance tracking API.
	ClassMirror byteBufferClass = vm.findBootstrapClassMirror(ByteBuffer.class.getName());
	MethodMirror method = HolographInternalUtils.getMethod(byteBufferClass, "allocate", vm.getPrimitiveClass("int"));
	return (InstanceMirror)HolographInternalUtils.mirrorInvoke(ThreadHolograph.currentThreadMirror(), method, null, 16);
    }
}
