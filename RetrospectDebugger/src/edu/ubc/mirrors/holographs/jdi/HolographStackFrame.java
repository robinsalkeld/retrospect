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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;

public class HolographStackFrame extends Holograph implements StackFrame {

    private final StackFrame wrapped;
    
    /**
     * @return
     * @see com.sun.jdi.StackFrame#getArgumentValues()
     */
    public List<Value> getArgumentValues() {
        return vm.wrapValues(wrapped.getArgumentValues());
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.StackFrame#getValue(com.sun.jdi.LocalVariable)
     */
    public Value getValue(LocalVariable arg0) {
        return vm.wrapValue(wrapped.getValue(arg0));
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.StackFrame#getValues(java.util.List)
     */
    public Map<LocalVariable, Value> getValues(List arg0) {
        Map<LocalVariable, Value> result = new HashMap<LocalVariable, Value>();
        for (Object o : wrapped.getValues(arg0).entrySet()) {
            Map.Entry entry = (Map.Entry)o;
            LocalVariable key = (LocalVariable)entry.getKey();
            Value value = (Value)entry.getValue();
            result.put(key, vm.wrapValue(value));
        }
        return result;
    }

    /**
     * @return
     * @see com.sun.jdi.StackFrame#location()
     */
    public Location location() {
        return new HolographLocation(vm, wrapped.location());
    }

    /**
     * @param arg0
     * @param arg1
     * @throws InvalidTypeException
     * @throws ClassNotLoadedException
     * @see com.sun.jdi.StackFrame#setValue(com.sun.jdi.LocalVariable, com.sun.jdi.Value)
     */
    public void setValue(LocalVariable arg0, Value arg1)
            throws InvalidTypeException, ClassNotLoadedException {
        wrapped.setValue(arg0, arg1);
    }

    /**
     * @return
     * @see com.sun.jdi.StackFrame#thisObject()
     */
    public ObjectReference thisObject() {
        return vm.wrapObjectReference(wrapped.thisObject());
    }

    /**
     * @return
     * @see com.sun.jdi.StackFrame#thread()
     */
    public ThreadReference thread() {
        return vm.wrapThread(wrapped.thread());
    }

    /**
     * @return
     * @see com.sun.jdi.Mirror#toString()
     */
    public String toString() {
        return wrapped.toString();
    }

    /**
     * @param arg0
     * @return
     * @throws AbsentInformationException
     * @see com.sun.jdi.StackFrame#visibleVariableByName(java.lang.String)
     */
    public LocalVariable visibleVariableByName(String arg0)
            throws AbsentInformationException {
        return wrapped.visibleVariableByName(arg0);
    }

    /**
     * @return
     * @throws AbsentInformationException
     * @see com.sun.jdi.StackFrame#visibleVariables()
     */
    public List<LocalVariable> visibleVariables()
            throws AbsentInformationException {
        return wrapped.visibleVariables();
    }

    public HolographStackFrame(JDIHolographVirtualMachine vm, StackFrame frame) {
        super(vm, frame);
        this.wrapped = frame;
    }

}
