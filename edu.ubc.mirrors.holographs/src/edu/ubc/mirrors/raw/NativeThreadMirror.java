package edu.ubc.mirrors.raw;

import java.util.List;

import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ThreadMirror;

public class NativeThreadMirror extends NativeInstanceMirror implements ThreadMirror {

    public NativeThreadMirror(Thread thread) {
        super(thread);
        this.thread = thread;
    }

    private final Thread thread;
    
    @Override
    public List<FrameMirror> getStackTrace() {
	// TODO-RS
	return null;
    }

    @Override
    public void interrupt() {
        thread.interrupt();
    }
    
}
