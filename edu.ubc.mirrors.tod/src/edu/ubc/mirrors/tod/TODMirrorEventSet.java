package edu.ubc.mirrors.tod;

import java.util.HashSet;

import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventSet;
import edu.ubc.mirrors.ThreadMirror;

public class TODMirrorEventSet extends HashSet<MirrorEvent> implements MirrorEventSet {

    private final TODVirtualMachineMirror vm;
    private ThreadMirror thread;
    
    private static final long serialVersionUID = 8455197622920137674L;

    public TODMirrorEventSet(TODVirtualMachineMirror vm) {
        this.vm = vm;
    }
    
    @Override
    public boolean add(MirrorEvent e) {
        if (thread == null) {
            thread = e.thread();
        } else if (!thread.equals(e.thread())) {
           throw new IllegalArgumentException("Wrong thread");
        }

        return super.add(e);
    }
    
    @Override
    public ThreadMirror thread() {
        return thread;
    }
    
    @Override
    public void resume() {
        // TODO-RS: Managing multiple threads correctly
        vm.resume();
    }
    
}
