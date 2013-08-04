package edu.ubc.mirrors.raw.nativestubs.java.util.concurrent.atomic;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class AtomicIntegerStubs extends NativeStubs {

    public AtomicIntegerStubs(ClassHolograph klass) {
	super(klass);
    }

    // TODO-RS: Complete UnsafeStubs and remove this higher-level version.
    // TODO-RS: Revisit when concurrent execution is supported.
    @StubMethod
    public boolean compareAndSet(InstanceMirror instance, int expect, int update) {
        try {
            FieldMirror field = klass.getDeclaredField("value");
            if (instance.getInt(field) == expect) {
                instance.setInt(field, update);
                return true;
            } else {
                return false;
            }
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError("value");
        }
    }
    
}
