package edu.ubc.mirrors.holograms;

import java.util.HashSet;
import java.util.Set;

import edu.ubc.mirrors.ObjectMirror;

public class HologramThread extends Thread {

    private Set<ObjectMirror> heldMonitors = new HashSet<ObjectMirror>();
    
    private Set<ObjectMirror> monitorsToHold = new HashSet<ObjectMirror>();
    
    @Override
    public void run() {
        for (;;) {
            synchronized(this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            
                for (ObjectMirror held : heldMonitors) {
                    if (!monitorsToHold.contains(held)) {
                        ObjectHologram hologram = (ObjectHologram)ObjectHologram.make(held);
                        hologram.getSynchronizationLock().unlock();
                    }
                }
                for (ObjectMirror toHold : monitorsToHold) {
                    if (!heldMonitors.contains(toHold)) {
                        ObjectHologram hologram = (ObjectHologram)ObjectHologram.make(toHold);
                        hologram.getSynchronizationLock().lock();
                    }
                }
                heldMonitors = monitorsToHold;
            
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
    }
    
}
