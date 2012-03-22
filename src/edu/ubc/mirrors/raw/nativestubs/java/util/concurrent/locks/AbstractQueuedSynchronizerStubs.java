package edu.ubc.mirrors.raw.nativestubs.java.util.concurrent.locks;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.mirages.Mirage;

public class AbstractQueuedSynchronizerStubs {

    public static boolean compareAndSetState(Class<?> classLoaderLiteral, Mirage thiz, int expect, int update) {
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
