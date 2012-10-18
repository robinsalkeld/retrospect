package edu.ubc.mirrors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import edu.ubc.mirrors.test.Breakpoint;

public class EventDispatch {

    public static interface EventCallback {
	
	public void handle(MirrorEvent event);
    }
    
    private static class CallbackThread extends Thread {
	
	private MirrorEvent event;
	private boolean stop = false;
	private Semaphore semaphore = new Semaphore(0);
	
	public void setEvent(MirrorEvent event) {
	    this.event = event;
	    semaphore.release();
	}
	
	@Override
	public void run() {
	    while (!stop) {
		try {
		    semaphore.acquire();
		} catch (InterruptedException e) {
		    throw new RuntimeException(e);
		}
		handleEvent(event);
	    }
	}
    }
    
    private static final Object CALLBACKS_KEY = new Object();
    
    private final VirtualMachineMirror vm;
    
    private final List<Runnable> eventSetCallbacks = new ArrayList<Runnable>();
    
    public EventDispatch(VirtualMachineMirror vm) {
	this.vm = vm;
    }
    
    @SuppressWarnings("unchecked")
    public void addCallback(MirrorEventRequest request, EventCallback callback) {
	List<EventCallback> callbacks = (List<EventCallback>)request.getProperty(CALLBACKS_KEY);
	if (callbacks == null) {
	    callbacks = new ArrayList<EventCallback>();
	    request.putProperty(CALLBACKS_KEY, callbacks);
	}
	callbacks.add(callback);
    }
    
    public void addSetCallback(Runnable callback) {
	eventSetCallbacks.add(callback);
    }
    
    public static void handleEvent(MirrorEvent event) {
	MirrorEventRequest request = event.request();
	if (request != null) {
	    List<?> callbacks = (List<?>)request.getProperty(CALLBACKS_KEY);
	    if (callbacks != null) {
		for (Object callback : callbacks) {
		    ((EventCallback)callback).handle(event);
		}
	    }
	} else {
	    Breakpoint.bp();
	}
    }
    
    public void start() throws InterruptedException {
	vm.resume();
	MirrorEventQueue q = vm.eventQueue();
	MirrorEventSet eventSet = q.remove();
	while (eventSet != null) {
	    System.out.println(eventSet);
	    for (MirrorEvent event : eventSet) {
		// TODO-RS: Temporary - this should be on a separate thread
		handleEvent(event);
	    }
	    for (Runnable setCallback : eventSetCallbacks) {
		setCallback.run();
	    }
	    
	    eventSet.resume();
	    eventSet = q.remove();
	}
    }
    
}
