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
package edu.ubc.mirrors.jdi;

import com.sun.jdi.request.MethodEntryRequest;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryRequest;

public class JDIMethodMirrorEntryRequest extends JDIEventRequest implements MethodMirrorEntryRequest {

    protected final MethodEntryRequest wrapped;
    protected MethodMirror methodFilter;

    public JDIMethodMirrorEntryRequest(JDIVirtualMachineMirror vm, MethodEntryRequest wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public void addClassFilter(ClassMirror klass) {
	JDIClassMirror declaringClass = (JDIClassMirror)klass;
	wrapped.addClassFilter(declaringClass.refType);
    }
    
    @Override
    public void setMethodFilter(MethodMirror methodFilter) {
	this.methodFilter = methodFilter;
	// Not supported directly, but adding a class filter helps to reduce excess events
	addClassFilter(methodFilter.getDeclaringClass());
    }
    
    @Override
    public void addClassFilter(String classNamePattern) {
	wrapped.addClassFilter(classNamePattern);
    }
}
