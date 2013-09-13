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
package edu.ubc.mirrors.asjdi;

import com.sun.jdi.request.EventRequest;

import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.asjdi.MirrorsMirror;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsEventRequest extends MirrorsMirror implements EventRequest {

    protected final MirrorEventRequest wrapped;
    
    protected static final String WRAPPER = "edu.ubc.mirrors.asjdi.wrapper";
    
    public MirrorsEventRequest(MirrorsVirtualMachine vm, MirrorEventRequest wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
        this.wrapped.putProperty(WRAPPER, this);
    }

    @Override
    public void addCountFilter(int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disable() {
        wrapped.disable();
    }

    @Override
    public void enable() {
        wrapped.enable();
    }

    @Override
    public Object getProperty(Object key) {
        return wrapped.getProperty(key);
    }

    @Override
    public boolean isEnabled() {
        return wrapped.isEnabled();
    }

    @Override
    public void putProperty(Object key, Object value) {
        wrapped.putProperty(key, value);
    }

    @Override
    public void setEnabled(boolean enabled) {
        wrapped.setEnabled(enabled);
    }

    @Override
    public void setSuspendPolicy(int arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public int suspendPolicy() {
        // TODO Auto-generated method stub
        return 0;
    }

}
