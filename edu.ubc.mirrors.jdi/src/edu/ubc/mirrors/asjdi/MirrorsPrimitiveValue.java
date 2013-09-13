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

import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.asjdi.MirrorsMirror;
import edu.ubc.mirrors.asjdi.MirrorsPrimitiveValue;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public abstract class MirrorsPrimitiveValue<T extends Number & Comparable<? super T>, U extends PrimitiveValue> extends MirrorsMirror implements PrimitiveValue {

    private final Type type;
    protected final T value;
    
    public MirrorsPrimitiveValue(MirrorsVirtualMachine vm, T wrapped) {
        super(vm, wrapped);
        ClassMirror primitiveClass = vm.vm.getPrimitiveClass(wrapped.getClass().getSimpleName().toLowerCase());
        this.type = vm.typeForClassMirror(primitiveClass);
        this.value = wrapped;
    }

    @Override
    public Type type() {
        return type;
    }
    
    @SuppressWarnings("unchecked")
    public int compareTo(U o) {
        // This is not fully general, but much easier to implement for now...
        return value.compareTo(((MirrorsPrimitiveValue<T, ?>)o).value);
    };
    
    @Override
    public boolean booleanValue() {
        return value.byteValue() != 0;
    }

    @Override
    public byte byteValue() {
        return value.byteValue();
    }

    @Override
    public char charValue() {
        return (char)value.intValue();
    }

    @Override
    public double doubleValue() {
        return value.doubleValue();
    }

    @Override
    public float floatValue() {
        return value.floatValue();
    }

    @Override
    public int intValue() {
        return value.intValue();
    }

    @Override
    public long longValue() {
        return value.longValue();
    }

    @Override
    public short shortValue() {
        return value.shortValue();
    }
}
