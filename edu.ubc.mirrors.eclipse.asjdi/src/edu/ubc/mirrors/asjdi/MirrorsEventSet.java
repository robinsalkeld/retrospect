package edu.ubc.mirrors.asjdi;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.request.EventRequest;

import edu.ubc.mirrors.MethodMirrorEntryEvent;
import edu.ubc.mirrors.MethodMirrorExitEvent;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventSet;
import edu.ubc.mirrors.asjdi.MirrorsMethodEntryEvent;
import edu.ubc.mirrors.asjdi.MirrorsMethodExitEvent;
import edu.ubc.mirrors.asjdi.MirrorsMirror;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsEventSet extends MirrorsMirror implements EventSet {

    private final MirrorEventSet wrapped;

    public MirrorsEventSet(MirrorsVirtualMachine vm, MirrorEventSet wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    private Set<Event> wrappedSet() {
        Set<Event> result = new HashSet<Event>(wrapped.size());
        for (MirrorEvent e : wrapped) {
            result.add(wrapEvent(e));
        }
        return result;
    }
    
    private Event wrapEvent(MirrorEvent e) {
        if (e instanceof MethodMirrorEntryEvent) {
            return new MirrorsMethodEntryEvent(vm, (MethodMirrorEntryEvent)e);
        } else if (e instanceof MethodMirrorExitEvent) {
            return new MirrorsMethodExitEvent(vm, (MethodMirrorExitEvent)e);
        } else {
            throw new IllegalArgumentException("Unrecognized event type: " + e);
        }
    }
    
    @Override
    public int size() {
        return wrapped.size();
    }

    @Override
    public boolean isEmpty() {
        return wrapped.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return wrappedSet().contains(o);
    }

    @Override
    public Iterator<Event> iterator() {
        return eventIterator();
    }

    @Override
    public Object[] toArray() {
        return wrappedSet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return wrappedSet().toArray(a);
    }

    @Override
    public boolean add(Event e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return wrappedSet().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Event> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EventIterator eventIterator() {
        final Iterator<Event> i = wrappedSet().iterator();
        return new EventIterator() {

            @Override
            public boolean hasNext() {
                return i.hasNext();
            }

            @Override
            public Event next() {
                return i.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Event nextEvent() {
                return i.next();
            }
            
        };
    }

    @Override
    public void resume() {
        wrapped.resume();
    }

    @Override
    public int suspendPolicy() {
        return EventRequest.SUSPEND_EVENT_THREAD;
    }

}
