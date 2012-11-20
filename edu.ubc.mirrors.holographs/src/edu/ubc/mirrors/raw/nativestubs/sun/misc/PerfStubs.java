package edu.ubc.mirrors.raw.nativestubs.sun.misc;

import java.nio.ByteBuffer;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.ObjectMirage;

public class PerfStubs extends NativeStubs {

    public PerfStubs(ClassHolograph klass) {
	super(klass);
    }

    public void registerNatives() {
	// No need to do anything
    }
    
    public Mirage createLong(Mirage perf, Mirage name, int derp, int florp, long blong) {
	VirtualMachineHolograph vm = getVM();
	
	ClassMirror byteBufferClass = vm.findBootstrapClassMirror(ByteBuffer.class.getName());
	MethodMirror method = HolographInternalUtils.getMethod(byteBufferClass, "allocate", vm.getPrimitiveClass("int"));
	ObjectMirror bb = (ObjectMirror)HolographInternalUtils.mirrorInvoke(ThreadHolograph.currentThreadMirror(), method, null, 16);
	return ObjectMirage.make(bb);
    }
}
