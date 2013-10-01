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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;

public class JDIFrameMirror extends JDIMirror implements FrameMirror {

    private final StackFrame frame;
    
    public JDIFrameMirror(JDIVirtualMachineMirror vm, StackFrame frame) {
	super(vm, frame);
	this.frame = frame;
    }

    @Override
    public ClassMirror declaringClass() {
        return vm.makeClassMirror(frame.location().declaringType());
    }
    
    @Override
    public String methodName() {
        return frame.location().method().name();
    }
    
    @Override
    public MethodMirror method() {
	return new JDIMethodMirror(vm, frame.location().method());
    }

    @Override
    public String fileName() {
	try {
            return frame.location().sourceName();
        } catch (AbsentInformationException e) {
            return "<Source Unknown>";
        }
    }

    @Override
    public int lineNumber() {
	return frame.location().lineNumber();
    }

    @Override
    public InstanceMirror thisObject() {
        return (InstanceMirror)vm.makeMirror(frame.thisObject());
    }
    
    @Override
    public List<Object> arguments() {
        // Don't call getArgumentValues() - that logs warnings like crazy if
        // local variable information is missing.
        List<LocalVariable> list;
        try {
            list = frame.location().method().variables();
        } catch (AbsentInformationException e) {
            return null;
        }
        ArrayList<Object> result = new ArrayList<Object>();
        for (LocalVariable var : list) {
            if (var.isArgument()) {
                result.add(vm.wrapValue(frame.getValue(var)));
            }
        }
        return result;
    }
}
