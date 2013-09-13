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

import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.InvalidRequestStateException;

public abstract class HolographEventRequest extends Holograph implements EventRequest {

    final EventRequest wrapped;
    
    public HolographEventRequest(JDIHolographVirtualMachine vm, ClassPrepareRequest wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }
    
    /**
     * @return
     * @see com.sun.jdi.Mirror#toString()
     */
    public String toString() {
        return wrapped.toString();
    }

    /**
     * @param arg1
     * @throws InvalidRequestStateException
     * @see com.sun.jdi.request.EventRequest#addCountFilter(int)
     */
    public void addCountFilter(int arg1) throws InvalidRequestStateException {
        wrapped.addCountFilter(arg1);
    }
    /**
     * 
     * @see com.sun.jdi.request.EventRequest#disable()
     */
    public void disable() {
        wrapped.disable();
    }
    /**
     * 
     * @see com.sun.jdi.request.EventRequest#enable()
     */
    public void enable() {
        wrapped.enable();
    }
    /**
     * @param key
     * @return
     * @see com.sun.jdi.request.EventRequest#getProperty(java.lang.Object)
     */
    public Object getProperty(Object key) {
        return wrapped.getProperty(key);
    }
    /**
     * @return
     * @see com.sun.jdi.request.EventRequest#isEnabled()
     */
    public boolean isEnabled() {
        return wrapped.isEnabled();
    }
    /**
     * @param key
     * @param value
     * @see com.sun.jdi.request.EventRequest#putProperty(java.lang.Object, java.lang.Object)
     */
    public void putProperty(Object key, Object value) {
        wrapped.putProperty(key, value);
    }
    /**
     * @param arg1
     * @see com.sun.jdi.request.EventRequest#setEnabled(boolean)
     */
    public void setEnabled(boolean arg1) {
        wrapped.setEnabled(arg1);
    }
    /**
     * @param arg1
     * @see com.sun.jdi.request.EventRequest#setSuspendPolicy(int)
     */
    public void setSuspendPolicy(int arg1) {
        wrapped.setSuspendPolicy(arg1);
    }
    /**
     * @return
     * @see com.sun.jdi.request.EventRequest#suspendPolicy()
     */
    public int suspendPolicy() {
        return wrapped.suspendPolicy();
    }
    
    
    
}
