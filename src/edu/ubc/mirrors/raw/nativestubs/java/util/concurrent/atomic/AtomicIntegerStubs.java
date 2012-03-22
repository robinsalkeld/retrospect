package edu.ubc.mirrors.raw.nativestubs.java.util.concurrent.atomic;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.mirages.Mirage;

public class AtomicIntegerStubs {

    public static boolean compareAndSet(Class<?> classLoaderLiteral, Mirage thiz, int expect, int update) {
        try {
            FieldMirror field = ((InstanceMirror)thiz.getMirror()).getMemberField("value");
            if (field.getInt() == expect) {
                field.setInt(update);
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
