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

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.Value;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.asjdi.MirrorsObjectReference;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsArrayReference extends MirrorsObjectReference implements ArrayReference {

    private final ArrayMirror wrapped;
    
    public MirrorsArrayReference(MirrorsVirtualMachine vm, ArrayMirror wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    @Override
    public Value getValue(int index) {
        return vm.valueForObject(Reflection.getArrayElement(wrapped, index));
    }

    @Override
    public List<Value> getValues() {
        int length = length();
        List<Value> result = new ArrayList<Value>(length);
        for (int i = 0; i < length; i++) {
            result.add(getValue(length));
        }
        return result;
    }

    @Override
    public List<Value> getValues(int index, int length) {
        List<Value> result = new ArrayList<Value>(length);
        for (int i = 0; i < length; i++) {
            result.add(getValue(index + i));
        }
        return result;
    }

    @Override
    public int length() {
        return wrapped.length();
    }

    @Override
    public void setValue(int index, Value value) throws InvalidTypeException, ClassNotLoadedException {
        Reflection.setArrayElement(wrapped, index, vm.objectForValue(value));
    }

    @Override
    public void setValues(List<? extends Value> values) throws InvalidTypeException, ClassNotLoadedException {
        // TODO-RS: Bah, no time...
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValues(int arg0, List<? extends Value> arg1, int arg2, int arg3) throws InvalidTypeException, ClassNotLoadedException {
        // TODO-RS: Bah, no time...
        throw new UnsupportedOperationException();
    }

}
