package edu.ubc.mirrors.raw.nativestubs.java.util.concurrent.locks;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class AbstractQueuedSynchronizerStubs extends NativeStubs {

    public AbstractQueuedSynchronizerStubs(ClassHolograph klass) {
	super(klass);
    }

    // TODO-RS: Complete UnsafeStubs and remove this higher-level version.
    // TODO-RS: Revisit when concurrent execution is supported.
    @StubMethod
    public boolean compareAndSetState(InstanceMirror instance, int expect, int update) {
        try {
            FieldMirror field = klass.getDeclaredField("state");
            if (instance.getInt(field) == expect) {
                instance.setInt(field, update);
                return true;
            } else {
                return false;
            }
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
        
}
