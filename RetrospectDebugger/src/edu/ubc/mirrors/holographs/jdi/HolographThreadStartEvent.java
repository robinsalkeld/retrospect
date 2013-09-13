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

import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.request.EventRequest;

public class HolographThreadStartEvent extends HolographEvent implements ThreadStartEvent {

    private final ThreadStartEvent wrapped;

    /**
     * @return
     * @see com.sun.jdi.Mirror#toString()
     */
    public String toString() {
        return wrapped.toString();
    }

    /**
     * @return
     * @see com.sun.jdi.event.ThreadStartEvent#thread()
     */
    public ThreadReference thread() {
        return vm.wrapThread(wrapped.thread());
    }

    /**
     * @return
     * @see com.sun.jdi.event.Event#request()
     */
    public EventRequest request() {
        return wrapped.request();
    }

    public HolographThreadStartEvent(JDIHolographVirtualMachine vm, ThreadStartEvent wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }
    
    
}
