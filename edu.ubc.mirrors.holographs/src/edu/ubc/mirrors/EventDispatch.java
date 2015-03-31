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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class EventDispatch {

    public static class EventDispatchThread extends Thread {
        
        private final EventDispatch dispatch;
        
        public EventDispatchThread(EventDispatch dispatch) {
            super("EventDispatchThread");
            this.dispatch = dispatch;
        }
        
        
        @Override
        public void run() {
            try {
                dispatch.run();
            } catch (InterruptedException e) {
            }
        }
    }

    private Comparator<MirrorEvent> REQUEST_ORDER_COMPARATOR = new Comparator<MirrorEvent>() {
        @Override
        public int compare(MirrorEvent left, MirrorEvent right) {
            return Integer.compare(getRequestOrder(left.request()),
                                   getRequestOrder(right.request()));
        }
    };
    
    private static final Object CALLBACKS_KEY = new Object();
    
    private final VirtualMachineMirror vm;
    
    private Callable<Object> eventSetCallback = null;
    private final Map<MirrorEventRequest, Integer> requestOrder = new HashMap<MirrorEventRequest, Integer>();
    private int nextOrder = 0;
    
    private boolean currentSetHandled = false;
    private MirrorEventSet currentSet = null;
    private List<MirrorEvent> pending = new ArrayList<MirrorEvent>();
    
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
	
        if (!requestOrder.containsKey(request)) {
            requestOrder.put(request, nextOrder++);
        }
    }
    
    private int getRequestOrder(MirrorEventRequest request) {
        Integer order = requestOrder.get(request);
        return order != null ? order : 0;
    }
    
    public void setEventSetCallback(Callable<Object> callback) {
	eventSetCallback = callback;
    }
    
    @SuppressWarnings("unchecked")
    public void handleEvent(MirrorEvent event) {
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
    
    @SuppressWarnings("unchecked")
    public MirrorInvocationHandler handleInvocableEvent(final InvocableMirrorEvent event) {
        MirrorEventRequest request = event.request();
        MirrorInvocationHandler proceed = event.getProceed();
        if (request != null) {
            List<?> callbacks = (List<?>)request.getProperty(CALLBACKS_KEY);
            if (callbacks != null) {
                for (Object callback : callbacks) {
                    final Callback<MirrorEvent> finalCallback = (Callback<MirrorEvent>)callback;
                    final MirrorInvocationHandler proceedBefore = proceed;
                    proceed = new MirrorInvocationHandler() {
                        public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
                            InvocableMirrorEvent newEvent = event.setProceed(proceedBefore, args);
                            return finalCallback.handle(newEvent);
                        }
                    };
                }
            }
        }
        return proceed;
    }
    
    public Object handleSetEvent() throws MirrorInvocationTargetException {
        try {
            return eventSetCallback.call();
        } catch (MirrorInvocationTargetException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void run() throws InterruptedException {
        runUntil(null);
    }
    
    public MirrorEvent runUntil(MirrorEventRequest request) throws InterruptedException {
        MirrorEvent event = nextEvent();
        while (event != null) {
            handleEvent(event);
            if (event.request().equals(request)) {
                return event;
            }
            
            event = nextEvent();
        }
        
        return null;
    }
    
    public MirrorEvent nextEvent() throws InterruptedException {
        while (pending.isEmpty()) {
            if (currentSet != null) {
                if (!currentSetHandled) {
                    currentSetHandled = true;
                    try {
                        handleSetEvent();
                    } catch (MirrorInvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
                currentSet.resume();
            }
            
            currentSet = vm.eventQueue().remove();
            currentSetHandled = false;
            if (currentSet == null) {
                return null;
            }
            
            pending.addAll(currentSet);
            Collections.sort(pending, REQUEST_ORDER_COMPARATOR);
        }
        
        return pending.remove(0);
    }
    
    public void forAllClasses(final Callback<ClassMirror> callback) {
        for (ClassMirror klass : vm.findAllClasses()) {
            callback.handle(klass);
        }
        ClassMirrorPrepareRequest request = vm.eventRequestManager().createClassMirrorPrepareRequest();
        addCallback(request, new Callback<MirrorEvent>() {
            public Object handle(MirrorEvent t) {
                ClassMirrorPrepareEvent event = (ClassMirrorPrepareEvent)t;
                callback.handle(event.classMirror());
                return null;
            }
        });
        request.enable();
    }
}

