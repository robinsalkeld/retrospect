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

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;

public class WrappingFrameMirror implements FrameMirror {

    public WrappingFrameMirror(WrappingVirtualMachine vm, FrameMirror wrapped) {
	this.vm = vm;
	this.wrapped = wrapped;
    }
    protected final WrappingVirtualMachine vm;
    protected final FrameMirror wrapped;
    
    @Override
    public ClassMirror declaringClass() {
        return (ClassMirror)vm.getWrappedMirror(wrapped.declaringClass());
    }
    @Override
    public String methodName() {
        return wrapped.methodName();
    }
    @Override
    public MethodMirror method() {
	MethodMirror method = wrapped.method();
        return method == null ? null : vm.wrapMethod(method);
    }
    @Override
    public ConstructorMirror constructor() {
        ConstructorMirror constructor = wrapped.constructor();
        return constructor == null ? null : vm.wrapConstructor(constructor);
    }
    @Override
    public String fileName() {
	return wrapped.fileName();
    }
    @Override
    public int lineNumber() {
	return wrapped.lineNumber();
    }
    
    @Override
    public InstanceMirror thisObject() {
        return (InstanceMirror)vm.getWrappedMirror(wrapped.thisObject());
    }
    
    @Override
    public List<Object> arguments() {
        List<Object> result = new ArrayList<Object>();
        List<Object> arguments = wrapped.arguments();
        if (arguments == null) {
            return null;
        }
        for (Object arg : arguments) {
            if (arg instanceof ObjectMirror) {
                result.add(vm.getWrappedMirror((ObjectMirror)arg));
            } else {
                result.add(arg);
            }
        }
        return result;
    }
    
}
