package edu.ubc.mirrors.raw.nativestubs.java.util.concurrent.atomic;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.mirages.Mirage;

public class AtomicIntegerStubs extends NativeStubs {

    public AtomicIntegerStubs(ClassHolograph klass) {
	super(klass);
    }

    public boolean compareAndSet(Mirage thiz, int expect, int update) {
        try {
            InstanceMirror instance = (InstanceMirror)thiz.getMirror();
            FieldMirror field = klass.getDeclaredField("value");
            if (field.getInt(instance) == expect) {
                field.setInt(instance, update);
                return true;
            } else {
                return false;
            }
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError("value");
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError("value");
        }
    }
    
}
