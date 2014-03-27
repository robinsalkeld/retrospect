package edu.ubc.mirrors.tod;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import edu.ubc.mirrors.MirrorEventQueue;
import edu.ubc.mirrors.MirrorEventSet;

public class TODMirrorEventQueue implements MirrorEventQueue {

    private final TODVirtualMachineMirror vm;
    
    private final BlockingQueue<TODMirrorEventSet> q = new LinkedBlockingQueue<TODMirrorEventSet>();
    
    public TODMirrorEventQueue(TODVirtualMachineMirror vm) {
        this.vm = vm;
    }

    public void addEventSet(TODMirrorEventSet set) {
        if (set == null) {
            set = new TODMirrorEventSet(vm);
        }
        q.add(set);
    }
    
    @Override
    public MirrorEventSet remove() throws InterruptedException {
        MirrorEventSet result = q.take();
        return result.isEmpty() ? null : result;
    }
}
