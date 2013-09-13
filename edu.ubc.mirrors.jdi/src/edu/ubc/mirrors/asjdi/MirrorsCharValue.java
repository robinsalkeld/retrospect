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

import com.sun.jdi.CharValue;
import com.sun.jdi.IntegerValue;

import edu.ubc.mirrors.asjdi.MirrorsPrimitiveValue;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsCharValue extends MirrorsPrimitiveValue<Integer, IntegerValue> implements CharValue {

    public MirrorsCharValue(MirrorsVirtualMachine vm, char value) {
        super(vm, (int)value);
    }

    @Override
    public int compareTo(CharValue o) {
        return value.compareTo(o.intValue());
    }
    
    @Override
    public char charValue() {
        return (char)value.intValue();
    }

    @Override
    public char value() {
        return charValue();
    }
}
