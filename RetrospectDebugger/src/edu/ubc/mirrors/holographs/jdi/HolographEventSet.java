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
package edu.ubc.mirrors.holographs.jdi;

import java.util.Collection;
import java.util.Iterator;

import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventSet;

public class HolographEventSet extends Holograph implements EventSet {

    private final EventSet wrapped;
    
    /**
     * @param o
     * @return
     * @see java.util.Set#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
        return wrapped.contains(vm.unwrapEvent((Event)o));
    }

    /**
     * @param e
     * @return
     * @see java.util.Set#add(java.lang.Object)
     */
    public boolean add(Event e) {
        return wrapped.add(vm.wrapEvent(e));
    }

    /**
     * @param o
     * @return
     * @see java.util.Set#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        return wrapped.remove(vm.unwrapEvent((Event)o));
    }

    /**
     * @param c
     * @return
     * @see java.util.Set#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param c
     * @return
     * @see java.util.Set#addAll(java.util.Collection)
     */
    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param c
     * @return
     * @see java.util.Set#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    /**
     * 
     * @see java.util.Set#clear()
     */
    public void clear() {
        wrapped.clear();
    }

    /**
     * @return
     * @see com.sun.jdi.event.EventSet#eventIterator()
     */
    public EventIterator eventIterator() {
        return new HolographEventIterator(wrapped.eventIterator());
    }

    public class HolographEventIterator implements EventIterator {
     
        final EventIterator wrapped;

        public HolographEventIterator(EventIterator wrapped) {
            this.wrapped = wrapped;
        }
        
        /**
         * @return
         * @see com.sun.jdi.event.EventIterator#nextEvent()
         */
        public Event nextEvent() {
            return vm.wrapEvent(wrapped.nextEvent());
        }

        /**
         * @return
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return wrapped.hasNext();
        }

        /**
         * @return
         * @see java.util.Iterator#next()
         */
        public Event next() {
            return nextEvent();
        }

        /**
         * 
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            wrapped.remove();
        }
        
        
        
    }
    
    /**
     * @return
     * @see java.util.Set#isEmpty()
     */
    public boolean isEmpty() {
        return wrapped.isEmpty();
    }

    /**
     * @return
     * @see java.util.Set#iterator()
     */
    public Iterator<Event> iterator() {
        return eventIterator();
    }

    /**
     * 
     * @see com.sun.jdi.event.EventSet#resume()
     */
    public void resume() {
        wrapped.resume();
    }

    /**
     * @return
     * @see java.util.Set#size()
     */
    public int size() {
        return wrapped.size();
    }

    /**
     * @param c
     * @return
     * @see java.util.Set#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return
     * @see com.sun.jdi.event.EventSet#suspendPolicy()
     */
    public int suspendPolicy() {
        return wrapped.suspendPolicy();
    }

    /**
     * @return
     * @see java.util.Set#toArray()
     */
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param a
     * @return
     * @see java.util.Set#toArray(T[])
     */
    public Object[] toArray(Object[] a) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return
     * @see com.sun.jdi.Mirror#toString()
     */
    public String toString() {
        return wrapped.toString();
    }

    public HolographEventSet(JDIHolographVirtualMachine vm, EventSet wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }
}
