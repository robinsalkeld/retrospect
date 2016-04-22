package edu.ubc.mirrors.holograms;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;

public class HologramThread extends Thread {

    private static boolean ENABLE = false;
    
    private final ThreadMirror thread;
    private Set<ObjectMirror> heldMonitors = new HashSet<ObjectMirror>();

    private Object target;
    private Method method;
    private Object[] args;
    
    private Throwable throwable = null;
    private Object result;
    
    public HologramThread(ThreadMirror thread) {
        this.thread = thread;
        setDaemon(true);
    }
    
    @Override
    public void run() {
        for (;;) {
            handleInvoke();
        }
    }
    
    private synchronized void handleInvoke() {
        this.result = null;
        this.throwable = null;
        
        Reflection.withThread(thread, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Set<ObjectMirror> monitorsToHold = new HashSet<ObjectMirror>(thread.getOwnedMonitors());
                
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
                HologramThread.this.heldMonitors = monitorsToHold;
                
                try {
                    HologramThread.this.result = method.invoke(target, args);
                } catch (Throwable t) {
                    HologramThread.this.throwable = t;
                }
                return null;
            }
        });
        
        notify();
        try {
            wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Object invoke(Object target, Method method, Object[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (!ENABLE || Thread.currentThread() == this) {
            return method.invoke(target, args);
        }
        
        synchronized(this) {
            this.target = target;
            this.method = method;
            this.args = args;
            
            if (isAlive()) {
                notify();
            } else {
                start();
            }
            
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            
            if (this.throwable != null) {
                if (throwable instanceof InvocationTargetException) {
                    throw (InvocationTargetException)throwable;
                } else {
                    throw (RuntimeException)throwable;
                }
            } else {
                return result;
            }
        }
    }
}
