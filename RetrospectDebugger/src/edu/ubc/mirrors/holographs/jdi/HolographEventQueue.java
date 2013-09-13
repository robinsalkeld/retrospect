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

import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;

public class HolographEventQueue extends Holograph implements EventQueue {

    private final EventQueue wrapped;

    public HolographEventQueue(JDIHolographVirtualMachine vm, EventQueue wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }
    
    /**
     * @return
     * @throws InterruptedException
     * @see com.sun.jdi.event.EventQueue#remove()
     */
    public EventSet remove() throws InterruptedException {
        return vm.wrapEventSet(wrapped.remove());
    }

    /**
     * @param arg0
     * @return
     * @throws InterruptedException
     * @see com.sun.jdi.event.EventQueue#remove(long)
     */
    public EventSet remove(long arg0) throws InterruptedException {
        return vm.wrapEventSet(wrapped.remove(arg0));
    }

    /**
     * @return
     * @see com.sun.jdi.Mirror#toString()
     */
    public String toString() {
        return wrapped.toString();
    }
}
