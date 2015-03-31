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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.ubc.mirrors.holographs.IllegalSideEffectError;

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

    private Comparator<MirrorEventRequest> REQUEST_ORDER_COMPARATOR = new Comparator<MirrorEventRequest>() {
        @Override
        public int compare(MirrorEventRequest left, MirrorEventRequest right) {
            return Integer.compare(getRequestOrder(left),
                                   getRequestOrder(right));
        }
    };
    
    private static final Object CALLBACKS_KEY = new Object();
    
    private final VirtualMachineMirror vm;
    
    private final Map<MirrorEventRequest, Integer> requestOrder = new HashMap<MirrorEventRequest, Integer>();
    private int nextOrder = 0;
    
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
    
    @SuppressWarnings("unchecked")
    private List<Callback<MirrorEvent>> getCallbacks(MirrorEventRequest request) {
        List<Callback<MirrorEvent>> callbacks = (List<Callback<MirrorEvent>>)request.getProperty(CALLBACKS_KEY);
        return callbacks == null ? Collections.<Callback<MirrorEvent>>emptyList() : callbacks;
    }
    
    private int getRequestOrder(MirrorEventRequest request) {
        Integer order = requestOrder.get(request);
        return order != null ? order : 0;
    }
    
    public void run() throws InterruptedException {
        runUntil(null);
    }
    
    public MirrorEvent runUntil(MirrorEventRequest endRequest) throws InterruptedException {
        MirrorEventSet eventSet = vm.eventQueue().remove();
        while (eventSet != null && eventSet.isEmpty()) {
            eventSet = vm.eventQueue().remove();

            MirrorEvent result = null;
            for (MirrorEvent event : eventSet) {
                if (event.request().equals(endRequest)) {
                    result = event;
                }
            }
            
            try {
                runCallbacks(eventSet);
            } catch (MirrorInvocationTargetException e) {
                throw new IllegalSideEffectError(e);
            }

            if (result != null) {
                return result;
            }
            
            eventSet.resume();
            eventSet = vm.eventQueue().remove();
        }
        
        return null;
    }
    
    public Object runCallbacks(Set<MirrorEvent> events) throws MirrorInvocationTargetException {
        List<MirrorEventRequest> requests = new ArrayList<MirrorEventRequest>();
        for (MirrorEvent event : events) {
            MirrorEventRequest request = event.request();
            requests.add(request);
        }
        Collections.sort(requests, REQUEST_ORDER_COMPARATOR);

        Set<Callback<MirrorEvent>> callbacks = new LinkedHashSet<Callback<MirrorEvent>>();
        for (MirrorEventRequest request : requests) {
            callbacks.addAll(getCallbacks(request));
        }
        
        MirrorEvent event = mergeEvents(events);
        for (Callback<MirrorEvent> callback : callbacks) {
            event = callback.handle(event);
        }
        
        if (event instanceof InvocableMirrorEvent) {
            InvocableMirrorEvent invocableEvent = (InvocableMirrorEvent)event;
            return invocableEvent.getProceed().invoke(invocableEvent.thread(), invocableEvent.arguments());
        } else {
            return null;
        }
    }
    
    private MirrorEvent mergeEvents(Set<MirrorEvent> events) {
        MirrorEvent merged = null;
        for (MirrorEvent event : events) {
            if (merged == null) {
                merged = event;
            } else {
                if (!merged.getClass().equals(event.getClass())) {
                    throw new IllegalArgumentException("Incompatible events: " + merged + ", " + event);
                }
                // TODO-RS: Complete this - should be equivalent to MirrorEventShadow.equals()
                throw new IllegalArgumentException();
            }
        }
        return merged;
    }
    
    public void forAllClasses(final Callback<ClassMirror> callback) {
        for (ClassMirror klass : vm.findAllClasses()) {
            callback.handle(klass);
        }
        ClassMirrorPrepareRequest request = vm.eventRequestManager().createClassMirrorPrepareRequest();
        addCallback(request, new Callback<MirrorEvent>() {
            public MirrorEvent handle(MirrorEvent t) {
                ClassMirrorPrepareEvent event = (ClassMirrorPrepareEvent)t;
                callback.handle(event.classMirror());
                return null;
            }
        });
        request.enable();
    }
}

