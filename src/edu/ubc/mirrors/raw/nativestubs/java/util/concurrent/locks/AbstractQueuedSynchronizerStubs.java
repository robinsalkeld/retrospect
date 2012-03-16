package edu.ubc.mirrors.raw.nativestubs.java.util.concurrent.locks;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;

public class AbstractQueuedSynchronizerStubs {

    public static boolean compareAndSetState(ObjectMirror thiss, int expect, int update) {
        try {
            FieldMirror field = ((InstanceMirror)thiss).getMemberField("state");
            if (field.getInt() == expect) {
                field.setInt(update);
                return true;
            } else {
                return false;
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
        
}
