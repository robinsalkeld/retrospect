package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ThreadMirror;

public class NativeThreadMirror extends NativeInstanceMirror implements ThreadMirror {

    public NativeThreadMirror(Thread thread) {
        super(thread);
        this.thread = thread;
    }

    private final Thread thread;
    
    @Override
    public ObjectArrayMirror getStackTrace() {
        StackTraceElement[] stackTrace = thread.getStackTrace();
        if (stackTrace.length == 0) {
            return null;
        } else {
            return (ObjectArrayMirror)makeMirror(stackTrace);
        }
    }

}
