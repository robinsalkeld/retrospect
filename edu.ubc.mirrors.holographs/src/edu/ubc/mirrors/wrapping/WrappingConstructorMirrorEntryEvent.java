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

import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorEntryEvent;
import edu.ubc.mirrors.ThreadMirror;

public class WrappingConstructorMirrorEntryEvent extends WrappingMirrorEvent implements ConstructorMirrorEntryEvent {

    private final ConstructorMirrorEntryEvent wrapped;
    
    public WrappingConstructorMirrorEntryEvent(WrappingVirtualMachine vm, ConstructorMirrorEntryEvent wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public ConstructorMirror constructor() {
	return vm.wrapConstructor(wrapped.constructor());
    }
    
    @Override
    public ThreadMirror thread() {
        return (ThreadMirror)vm.getWrappedMirror(wrapped.thread());
    }
    
    @Override
    public List<Object> arguments() {
        List<Object> result = new ArrayList<Object>();
        for (Object a : wrapped.arguments()) {
            result.add(vm.wrapValue(a));
        }
        return result;
    }
    
    @Override
    public boolean isConstructorChaining() {
        return wrapped.isConstructorChaining();
    }
}
