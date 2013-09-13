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

import java.util.HashMap;
import java.util.Map;

import edu.ubc.mirrors.MirrorEventRequest;

public class WrappingMirrorEventRequest implements MirrorEventRequest {
    protected final WrappingVirtualMachine vm;
    private final MirrorEventRequest wrapped;
    private final Map<Object, Object> properties = new HashMap<Object, Object>();
    
    protected static final String WRAPPER = "edu.ubc.mirrors.wrapping.wrapper";
    
    public WrappingMirrorEventRequest(WrappingVirtualMachine vm, MirrorEventRequest wrapped) {
	this.vm = vm;
	this.wrapped = wrapped;
	wrapped.putProperty(WRAPPER, this);
    }

    @Override
    public void enable() {
	wrapped.enable();
    }

    @Override
    public void disable() {
	wrapped.disable();
    }

    @Override
    public boolean isEnabled() {
        return wrapped.isEnabled();
    }
    
    @Override
    public void setEnabled(boolean enabled) {
	wrapped.setEnabled(enabled);
    }

    @Override
    public Object getProperty(Object key) {
	return properties.get(key);
    }

    @Override
    public void putProperty(Object key, Object value) {
	properties.put(key, value);
    }

    @Override
    public void addClassFilter(String classNamePattern) {
	wrapped.addClassFilter(classNamePattern);
    }
}
