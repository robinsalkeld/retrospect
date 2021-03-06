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

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ObjectMirror;

public class WrappingMirror implements ObjectMirror {

    protected final WrappingVirtualMachine vm;
    // See WrappingClassMirror.wrapped for why this is not final.
    protected ObjectMirror wrapped;
    
    public WrappingMirror(WrappingVirtualMachine vm, ObjectMirror wrapped) {
        this.vm = vm;
        this.wrapped = wrapped;
        if (wrapped != null && vm.getWrappedVM() != wrapped.getClassMirror().getVM()) {
            throw new IllegalArgumentException();
        }
    }
    
    @Override
    public final boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        
        return ((WrappingMirror)obj).wrapped.equals(wrapped);
    }
    
    @Override
    public final int hashCode() {
        return 47 * wrapped.hashCode();
    }
    
    public ObjectMirror getWrapped() {
        return wrapped;
    }
    
    
    @Override
    public WrappingClassMirror getClassMirror() {
        return vm.getWrappedClassMirror(wrapped.getClassMirror());
    }
    
    @Override
    public int identityHashCode() {
        return wrapped.identityHashCode();
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        if (wrapped instanceof ArrayMirror) {
            return vm.getWrappedMirror((ObjectMirror)((ArrayMirror)wrapped).clone());
        } else {
            throw new CloneNotSupportedException();
        }
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + wrapped;
    }
    
    public void setWrapped(ObjectMirror wrapped) {
        this.wrapped = wrapped;
    }
    
    @Override
    public void allowCollection(boolean flag) {
        wrapped.allowCollection(flag);
    }
    
    @Override
    public boolean isCollected() {
        return wrapped.isCollected();
    }
}
