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

import com.sun.jdi.Mirror;
import com.sun.jdi.VirtualMachine;

import edu.ubc.mirrors.asjdi.MirrorsMirror;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public abstract class MirrorsMirror implements Mirror {

    protected final MirrorsVirtualMachine vm;
    protected final Object wrapped;
    
    public MirrorsMirror(MirrorsVirtualMachine vm, Object wrapped) {
        this.vm = vm;
        this.wrapped = wrapped;
    }

    @Override
    public VirtualMachine virtualMachine() {
        return vm;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        
        return ((MirrorsMirror)obj).wrapped.equals(wrapped);
    }
    
    @Override
    public int hashCode() {
        return 47 * wrapped.hashCode();
    }
    
    public Object getWrapped() {
        return wrapped;
    }
}
