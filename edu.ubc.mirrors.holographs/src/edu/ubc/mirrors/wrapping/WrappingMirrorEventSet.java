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
package edu.ubc.mirrors.wrapping;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.ubc.mirrors.ClassMirrorPrepareEvent;
import edu.ubc.mirrors.ConstructorMirrorEntryEvent;
import edu.ubc.mirrors.ConstructorMirrorExitEvent;
import edu.ubc.mirrors.FieldMirrorGetEvent;
import edu.ubc.mirrors.FieldMirrorSetEvent;
import edu.ubc.mirrors.MethodMirrorEntryEvent;
import edu.ubc.mirrors.MethodMirrorExitEvent;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventSet;
import edu.ubc.mirrors.MirrorLocationEvent;
import edu.ubc.mirrors.ThreadMirror;

public class WrappingMirrorEventSet implements MirrorEventSet {

    private final WrappingVirtualMachine vm;
    private final MirrorEventSet wrapped;

    public WrappingMirrorEventSet(WrappingVirtualMachine vm, MirrorEventSet wrapped) {
	this.vm = vm;
	this.wrapped = wrapped;
    }

    private Set<MirrorEvent> wrappedSet() {
	Set<MirrorEvent> result = new HashSet<MirrorEvent>(wrapped.size());
	for (MirrorEvent e : wrapped) {
	    MirrorEvent wrappedEvent = wrapEvent(e);
	    result.add(wrappedEvent);
	}
	return result;
    }
    
    private MirrorEvent wrapEvent(MirrorEvent e) {
	if (e instanceof MethodMirrorEntryEvent) {
	    return new WrappingMethodMirrorEntryEvent(vm, (MethodMirrorEntryEvent)e);
	} else if (e instanceof MethodMirrorExitEvent) {
	    return new WrappingMethodMirrorExitEvent(vm, (MethodMirrorExitEvent)e);
	} else if (e instanceof ConstructorMirrorEntryEvent) {
	    return new WrappingConstructorMirrorEntryEvent(vm, (ConstructorMirrorEntryEvent)e);
	} else if (e instanceof ConstructorMirrorExitEvent) {
	    return new WrappingConstructorMirrorExitEvent(vm, (ConstructorMirrorExitEvent)e);
	} else if (e instanceof FieldMirrorGetEvent) {
	    return new WrappingFieldMirrorGetEvent(vm, (FieldMirrorGetEvent)e);
	} else if (e instanceof FieldMirrorSetEvent) {
            return new WrappingFieldMirrorSetEvent(vm, (FieldMirrorSetEvent)e);
        } else if (e instanceof ClassMirrorPrepareEvent) {
	    return new WrappingClassMirrorPrepareEvent(vm, (ClassMirrorPrepareEvent)e);
        } else if (e instanceof MirrorLocationEvent) {
            return new WrappingMirrorLocationEvent(vm, (MirrorLocationEvent)e);
        } else {
	    throw new IllegalArgumentException("Unrecognized event type: " + e);
	}
    }

    /**
     * @return
     * @see java.util.Set#size()
     */
    public int size() {
	return wrapped.size();
    }

    /**
     * @return
     * @see java.util.Set#isEmpty()
     */
    public boolean isEmpty() {
	return wrapped.isEmpty();
    }

    /**
     * @param o
     * @return
     * @see java.util.Set#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
	return wrappedSet().contains(o);
    }

    /**
     * @return
     * @see java.util.Set#iterator()
     */
    public Iterator<MirrorEvent> iterator() {
	return wrappedSet().iterator();
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
	return wrappedSet().containsAll(c);
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
	wrapped.resume();
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
	return wrappedSet().toArray();
    }

    /**
     * @param a
     * @return
     * @see java.util.Set#toArray(T[])
     */
    public <T> T[] toArray(T[] a) {
	return wrappedSet().toArray(a);
    }
    
    @Override
    public ThreadMirror thread() {
        return (ThreadMirror)vm.getWrappedMirror(wrapped.thread());
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + wrapped;
    }
}
