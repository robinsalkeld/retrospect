package edu.ubc.mirrors.holograms;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;

public class HologramThread extends Thread {

    private final ThreadMirror thread;
    
    private Set<ObjectMirror> heldMonitors = new HashSet<ObjectMirror>();
    
    private Set<ObjectMirror> monitorsToHold = new HashSet<ObjectMirror>();
    
    private RuntimeException exception = null;
    
    public HologramThread(ThreadMirror thread) {
        this.thread = thread;
        setDaemon(true);
    }
    
    @Override
    public void run() {
        for (;;) {
            synchronized(this) {
                try {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                
                    Reflection.withThread(thread, new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            for (ObjectMirror held : heldMonitors) {
                                if (!monitorsToHold.contains(held)) {
                                    Hologram hologram = (Hologram)ObjectHologram.make(held);
                                    hologram.getSynchronizationLock().unlock();
                                }
                            }
                            for (ObjectMirror toHold : monitorsToHold) {
                                if (!heldMonitors.contains(toHold)) {
                                    Hologram hologram = (Hologram)ObjectHologram.make(toHold);
                                    hologram.getSynchronizationLock().lock();
                                }
                            }
                            return null;
                        }
                    });
                    
                    heldMonitors = monitorsToHold;
                } catch (RuntimeException e) {
                    exception = e;
                }
                
                notify();
            }
        }
    }
    
    public synchronized void setMonitors(Set<ObjectMirror> monitors) {
        this.monitorsToHold = monitors;
        notify();
        try {
            wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        
        try {
            if (this.exception != null ) {
                throw new RuntimeException(exception);
            }
        } finally {
            this.exception = null;
        }
    }
    
}
