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

import java.util.List;

import com.sun.jdi.request.MethodEntryRequest;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryRequest;

public class JDIMethodMirrorEntryRequest extends JDIEventRequest implements MethodMirrorEntryRequest {

    protected final MethodEntryRequest wrapped;
    protected String declaringClassFilter;
    protected String nameFilter;
    protected List<String> parameterTypeNamesFilter;

    public JDIMethodMirrorEntryRequest(JDIVirtualMachineMirror vm, MethodEntryRequest wrapped) {
	super(vm, wrapped);
	this.wrapped = wrapped;
    }

    @Override
    public void addClassFilter(ClassMirror klass) {
        if (klass instanceof JDIClassMirror) {
            JDIClassMirror declaringClass = (JDIClassMirror)klass;
            wrapped.addClassFilter(declaringClass.refType);
        } else {
            wrapped.addClassFilter(klass.getClassName());
        }
    }
    @Override
    public void setMethodFilter(String declaringClass, String name, List<String> parameterTypeNames) {
        this.declaringClassFilter = declaringClass;
        this.nameFilter = name;
        this.parameterTypeNamesFilter = parameterTypeNames;
        
        // Not supported directly, but adding a class filter helps to reduce excess events
	addClassFilter(declaringClass);
    }
    
    @Override
    public void addClassFilter(String classNamePattern) {
	wrapped.addClassFilter(classNamePattern);
    }
}
