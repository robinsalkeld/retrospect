package edu.ubc.mirrors.holograms;

import java.util.HashSet;
import java.util.Set;

public class HologramThread extends Thread {

    private Set<ObjectHologram> heldMonitors = new HashSet<ObjectHologram>();
    
    private Set<ObjectHologram> monitorsToHold = new HashSet<ObjectHologram>();
    
    @Override
    public void run() {
        for (;;) {
            synchronized(this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            
                for (ObjectHologram held : heldMonitors) {
                    if (!monitorsToHold.contains(held)) {
                        held.monitorExit();
                    }
                }
                for (ObjectHologram toHold : monitorsToHold) {
                    if (!heldMonitors.contains(toHold)) {
                        toHold.monitorEnter();
                    }
                }
                heldMonitors = monitorsToHold;
            
                notify();
            }
        }
    }
    
    public synchronized void setMonitors(Set<ObjectHologram> monitors) {
        this.monitorsToHold = monitors;
        notify();
        try {
            wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
}
