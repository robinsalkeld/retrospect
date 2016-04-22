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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import edu.ubc.mirrors.holographs.IllegalSideEffectError;

public class EventDispatch {

    private static final boolean DEBUG = Boolean.getBoolean("edu.ubc.mirrors.holographs.debugEvents"); 
    
    private int depth = 0;
    
    private int eventDepthOffset(MirrorEvent event) {
        if (event instanceof MethodMirrorEntryEvent || event instanceof ConstructorMirrorEntryEvent) {
            return 1;
        } else if (event instanceof MethodMirrorExitEvent || event instanceof ConstructorMirrorExitEvent) {
            return -1;
        } else {
            return 0;
        }
    }
    
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
//            } catch (TimeoutException e) {
                // This can happen if the VM ends while this thread is running
            } catch (InterruptedException e) {
            }
        }
    }

    public static class RaisedEventSet extends HashSet<MirrorEvent> implements MirrorEventSet {
        
        private final MirrorEventSet previous;
        
        public RaisedEventSet(MirrorEventSet previous) {
            this.previous = previous;
        }
        
        @Override
        public ThreadMirror thread() {
            return previous.thread();
        }
        
        @Override
        public void resume() {
            previous.resume();
        }
    }
    
    private Comparator<MirrorEventRequest> REQUEST_ORDER_COMPARATOR = new Comparator<MirrorEventRequest>() {
        @Override
        public int compare(MirrorEventRequest left, MirrorEventRequest right) {
            return Integer.compare(getRequestOrder(left),
                                   getRequestOrder(right));
        }
    };
    
    private Comparator<MirrorEventRequest> REVERSE_REQUEST_ORDER_COMPARATOR = Collections.reverseOrder(REQUEST_ORDER_COMPARATOR);
    
    private static final Object CALLBACKS_KEY = EventDispatch.class.getName() + ".callbacks";
    
    private final VirtualMachineMirror vm;
    
    private final Map<MirrorEventRequest, Integer> requestOrder = new HashMap<MirrorEventRequest, Integer>();
    private int nextOrder = 0;
    private MirrorEventSet currentSet = null;
    private MirrorEventSet pendingEvents;
    private boolean started = false;
    
    private List<Callback<Set<MirrorEvent>>> eventSetCallbacks = 
            new ArrayList<Callback<Set<MirrorEvent>>>();
    
    public EventDispatch(VirtualMachineMirror vm) {
	this.vm = vm;
    }
    
    public void raiseEvent(MirrorEvent event) {
        if (DEBUG) {
            printIndented("Raising event: " + event);
        }

        if (pendingEvents == null) {
            pendingEvents = new RaisedEventSet(currentSet);
        }
        pendingEvents.add(event);
    }
    
    @SuppressWarnings("unchecked")
    public void addCallback(MirrorEventRequest request, Callback<MirrorEvent> callback) {
	if (DEBUG) {
	    printIndented("Adding callback for: " + request);
	}
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
    
    public void addSetCallback(Callback<Set<MirrorEvent>> callback) {
        eventSetCallbacks.add(callback);
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
    
    public MirrorEventSet nextEventSet() throws InterruptedException {
        if (pendingEvents != null) {
            if (DEBUG) {
                printIndented("Handling raised events: " + pendingEvents);
            }
            
            currentSet = pendingEvents;
            pendingEvents = null;
            return currentSet;
        }
        
        if (started && currentSet != null) {
            currentSet.resume();
        } else {
            started = true;
        }
        
        currentSet = vm.eventQueue().remove();
        while (currentSet != null) {
            if (!currentSet.isEmpty()) {
                return currentSet;
            }
            
            currentSet.resume();
            currentSet = vm.eventQueue().remove();
        }
        
        return null;
    }
    
    public MirrorEvent runUntil(MirrorEventRequest endRequest) throws InterruptedException {
        if (DEBUG) {
            String endStr = endRequest == null ? "(none)" : endRequest.toString();
            printIndented("Running until: " + endStr);
        }
        
        currentSet = nextEventSet();
        while (currentSet != null) {
            MirrorEvent result = null;
            for (MirrorEvent event : currentSet) {
                if (endRequest != null && endRequest.equals(event.request())) {
                    if (DEBUG) {
                        printIndented("Hit matching event: " + event);
                    }
                    
                    result = event;
                }
            }
            
            try {
                runCallbacks(currentSet);
            } catch (MirrorInvocationTargetException e) {
                throw new IllegalSideEffectError(e);
            }
    
            if (result != null) {
                return result;
            }
            
            currentSet = nextEventSet();
        }
        
        if (endRequest != null) {
            throw new IllegalArgumentException("Never saw matching event for request: " + endRequest);
        }
        
        return null;
    }
    
    public Object runCallbacks(Set<MirrorEvent> events) throws MirrorInvocationTargetException {
        for (Callback<Set<MirrorEvent>> callback : eventSetCallbacks) {
            callback.handle(events);
        }
        
        List<MirrorEventRequest> requests = new ArrayList<MirrorEventRequest>();
        for (MirrorEvent event : events) {
            MirrorEventRequest request = event.request();
            if (request != null) {
                requests.add(request);
            }
        }
        
        Set<Callback<MirrorEvent>> callbacks = new LinkedHashSet<Callback<MirrorEvent>>();
        for (MirrorEventRequest request : requests) {
            callbacks.addAll(getCallbacks(request));
        }
        
        MirrorEvent event = mergeEvents(events);
        if (event == null) {
            return null;
        }
        
        // TODO-RS: Generalize
        if (event instanceof ConstructorMirrorExitEvent || event instanceof MethodMirrorExitEvent) {
            Collections.sort(requests, REVERSE_REQUEST_ORDER_COMPARATOR);
        } else {
            Collections.sort(requests, REQUEST_ORDER_COMPARATOR);
        }

        if (DEBUG) {
            int offset = eventDepthOffset(event);
            String msg = event.toString();
            if (offset > 0) {
                msg = "->" + msg;
            } else if (offset < 0) {
                depth += offset;
                msg = "<-" + msg;
            }
            printIndented(msg);
            if (offset > 0) {
                depth += offset;
            }
        }
        
        for (Callback<MirrorEvent> callback : callbacks) {
            event = callback.handle(event);
            if (event == null) {
                throw new NullPointerException();
            }
        }
        
        return event.getProceed().invoke(event.thread(), event.arguments());
    }
    
    private void printIndented(String msg) {
        for (int indent = 0; indent < depth; indent++) {
            System.err.print("  ");
        }
        System.err.println(msg);
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
            public MirrorEvent handle(final MirrorEvent t) {
                final ClassMirrorPrepareEvent event = (ClassMirrorPrepareEvent)t;
                Reflection.withThread(t.thread(), new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        callback.handle(event.classMirror());
                        return null;
                    }
                });
                return t;
            }
        });
        request.enable();
    }
}

