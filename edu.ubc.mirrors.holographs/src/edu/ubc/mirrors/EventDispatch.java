/*******************************************************************************
 * Copyright (c) 2013 Robin Salkeld
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package edu.ubc.mirrors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class EventDispatch {

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
    public void addCallback(MirrorEventRequest request, Callback<MirrorEvent> callback) {
	List<Callback<MirrorEvent>> callbacks = (List<Callback<MirrorEvent>>)request.getProperty(CALLBACKS_KEY);
	if (callbacks == null) {
	    callbacks = new ArrayList<Callback<MirrorEvent>>();
	    request.putProperty(CALLBACKS_KEY, callbacks);
	}
	callbacks.add(callback);
    }
    
    public void addSetCallback(Runnable callback) {
	eventSetCallbacks.add(callback);
    }
    
    @SuppressWarnings("unchecked")
    public static void handleEvent(MirrorEvent event) {
	MirrorEventRequest request = event.request();
	if (request != null) {
	    List<?> callbacks = (List<?>)request.getProperty(CALLBACKS_KEY);
	    if (callbacks != null) {
		for (Object callback : callbacks) {
		    ((Callback<MirrorEvent>)callback).handle(event);
		}
	    }
	}
    }
    
    public void start() throws InterruptedException {
	vm.resume();
	MirrorEventQueue q = vm.eventQueue();
	MirrorEventSet eventSet = q.remove();
	while (eventSet != null) {
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
    
    public void forAllClasses(final Callback<ClassMirror> callback) {
        for (ClassMirror klass : vm.findAllClasses()) {
            callback.handle(klass);
        }
        ClassMirrorPrepareRequest request = vm.eventRequestManager().createClassMirrorPrepareRequest();
        addCallback(request, new Callback<MirrorEvent>() {
            public void handle(MirrorEvent t) {
                ClassMirrorPrepareEvent event = (ClassMirrorPrepareEvent)t;
                callback.handle(event.classMirror());
            }
        });
    }
}
