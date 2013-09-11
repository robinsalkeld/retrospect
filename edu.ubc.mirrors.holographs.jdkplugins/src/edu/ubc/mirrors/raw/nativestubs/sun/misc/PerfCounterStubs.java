package edu.ubc.mirrors.raw.nativestubs.sun.misc;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

/**
 * This class makes use of direct memory buffers to make performance counters
 * available outside the process. Since we don't care about that for holographic 
 * VMs, it's simpler to just no-op out the operations.
 *
 */
public class PerfCounterStubs extends NativeStubs {

    public PerfCounterStubs(ClassHolograph klass) {
        super(klass);
    }

    @StubMethod
    public long get(InstanceMirror counter) {
        return 0;
    }
    
    @StubMethod
    public void set(InstanceMirror counter, long value) {
        // Ignore
    }
    
    @StubMethod
    public void add(InstanceMirror counter, long value) {
        // Ignore
    }
}
