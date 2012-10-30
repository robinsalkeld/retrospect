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
    public boolean compareAndSetState(Mirage thiz, int expect, int update) {
        try {
            FieldMirror field = ((InstanceMirror)thiz.getMirror()).getMemberField("state");
            if (field.getInt() == expect) {
                field.setInt(update);
                return true;
            } else {
                return false;
            }
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }
        
}
