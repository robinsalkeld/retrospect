package edu.ubc.mirrors.jdi;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import edu.ubc.mirrors.MirrorEventQueue;
import edu.ubc.mirrors.MirrorEventSet;

public class JDIMirrorEventQueueBuffer implements MirrorEventQueue {

    private final MirrorEventQueue wrapped;
    private final BlockingQueue<MirrorEventSet> q = new LinkedBlockingQueue<MirrorEventSet>();
    
    public JDIMirrorEventQueueBuffer(MirrorEventQueue wrapped) {
        this.wrapped = wrapped;
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    MirrorEventSet set = JDIMirrorEventQueueBuffer.this.wrapped.remove();
                    while (set != null) {
                        System.out.println("Queued: " + set);
                        q.add(set);
                        set = JDIMirrorEventQueueBuffer.this.wrapped.remove();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        thread.start();
    }
    
    @Override
    public MirrorEventSet remove() throws InterruptedException {
        MirrorEventSet result = q.remove();
        System.out.println("Removed: " + result);
        return result;
    }

}
