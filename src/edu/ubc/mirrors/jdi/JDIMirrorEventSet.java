package edu.ubc.mirrors.jdi;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.request.MethodEntryRequest;

import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventSet;

public class JDIMirrorEventSet implements MirrorEventSet {

    private final EventSet eventSet;

    public JDIMirrorEventSet(EventSet eventSet) {
	this.eventSet = eventSet;
    }

    private Set<MirrorEvent> toMirrorSet() {
	Set<MirrorEvent> result = new HashSet<MirrorEvent>(eventSet.size());
	for (Event e : eventSet) {
	    MirrorEvent wrapped = wrapEvent(e);
	    if (wrapped != null) {
		result.add(wrapped);
	    }
	}
	return result;
    }
    
    private MirrorEvent wrapEvent(Event e) {
	if (e instanceof MethodEntryEvent) {
	    MethodEntryEvent mee = (MethodEntryEvent)e;
	    JDIMethodMirrorEntryRequest request = (JDIMethodMirrorEntryRequest)mee.request().getProperty(JDIEventRequest.MIRROR_WRAPPER);
	    
	    
	}
	// TODO Auto-generated method stub
	return null;
    }

    /**
     * @return
     * @see java.util.Set#size()
     */
    public int size() {
	return eventSet.size();
    }

    /**
     * @return
     * @see java.util.Set#isEmpty()
     */
    public boolean isEmpty() {
	return eventSet.isEmpty();
    }

    /**
     * @param o
     * @return
     * @see java.util.Set#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
	return eventSet.contains(o);
    }

    /**
     * @return
     * @see java.util.Set#iterator()
     */
    public Iterator<MirrorEvent> iterator() {
	return eventSet.iterator();
    }

    /**
     * @param e
     * @return
     * @see java.util.Set#add(java.lang.Object)
     */
    public boolean add(MirrorEvent e) {
	throw new UnsupportedOperationException();
    }

    /**
     * @param o
     * @return
     * @see java.util.Set#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
	throw new UnsupportedOperationException();
    }

    /**
     * @param c
     * @return
     * @see java.util.Set#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection<?> c) {
	return eventSet.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends MirrorEvent> c) {
	throw new UnsupportedOperationException();
    }

    /**
     * @param c
     * @return
     * @see java.util.Set#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection<?> c) {
	throw new UnsupportedOperationException();
    }

    /**
     * 
     * @see java.util.Set#clear()
     */
    public void clear() {
	throw new UnsupportedOperationException();
    }

    /**
     * 
     * @see com.sun.jdi.event.EventSet#resume()
     */
    public void resume() {
	eventSet.resume();
    }

    /**
     * @param c
     * @return
     * @see java.util.Set#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection<?> c) {
	throw new UnsupportedOperationException();
    }

    /**
     * @return
     * @see java.util.Set#toArray()
     */
    public Object[] toArray() {
	return eventSet.toArray();
    }

    /**
     * @param a
     * @return
     * @see java.util.Set#toArray(T[])
     */
    public <T> T[] toArray(T[] a) {
	return eventSet.toArray(a);
    }
}
