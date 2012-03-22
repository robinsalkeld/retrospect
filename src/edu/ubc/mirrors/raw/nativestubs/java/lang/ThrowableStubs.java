package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.raw.NativeObjectMirror;

public class ThrowableStubs {

//    public static Mirage getOurStackTrace(Class<?> classLoaderLiteral, Mirage throwable) {
//        try {
//            InstanceMirror throwableMirror = (InstanceMirror)throwable.getMirror();
//            ObjectMirror fieldValue = throwableMirror.getMemberField("stackTrace").get();
//            if (fieldValue == null) {
//                StackTraceElement[] nativeTrace = ((Throwable)throwable).getStackTrace();
//                StackTraceElement[] correctedTrace = new StackTraceElement[nativeTrace.length];
//                for (int i = 0; i < nativeTrace.length; i++) {
//                    correctedTrace[i] = ObjectMirage.cleanStackTraceElement(nativeTrace[i]);
//                }
//                fieldValue = NativeObjectMirror.makeMirror(correctedTrace);
//                throwableMirror.getMemberField("stackTrace").set(fieldValue);
//            }
//            return (Mirage)ObjectMirage.make(fieldValue, classLoaderLiteral);
//        } catch (NoSuchFieldException e) {
//            throw new NoSuchFieldError(e.getMessage());
//        } catch (IllegalAccessException e) {
//            throw new IllegalAccessError(e.getMessage());
//        }
//    }
}
