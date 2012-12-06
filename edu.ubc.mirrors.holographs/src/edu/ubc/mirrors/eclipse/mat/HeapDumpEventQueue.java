package edu.ubc.mirrors.eclipse.mat;

import edu.ubc.mirrors.MirrorEventQueue;
import edu.ubc.mirrors.MirrorEventSet;

public class HeapDumpEventQueue implements MirrorEventQueue {

    public HeapDumpEventQueue(HeapDumpVirtualMachineMirror heapDumpVirtualMachineMirror) {
    }

    @Override
    public synchronized MirrorEventSet remove() throws InterruptedException {
        wait();
        return null;
    }

}
