package edu.ubc.mirrors.raw.nativestubs.java.util.concurrent.locks;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.mirages.Mirage;

public class AbstractQueuedSynchronizerStubs extends NativeStubs {

    public AbstractQueuedSynchronizerStubs(ClassHolograph klass) {
	super(klass);
    }

    // TODO-RS: Needs to be made thread-safe!
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
