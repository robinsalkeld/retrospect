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
package edu.ubc.mirrors.fieldmap;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;

public abstract class CalculatedObjectArrayMirror implements ObjectArrayMirror {

    private final ClassMirror klass;
    private final int length;
    
    public CalculatedObjectArrayMirror(ClassMirror klass, int length) {
        this.klass = klass;
        this.length = length;
    }
    
    @Override
    public ClassMirror getClassMirror() {
        return klass;
    }

    @Override
    public int identityHashCode() {
        return hashCode();
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public abstract ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException;

    @Override
    public void set(int index, ObjectMirror o) throws ArrayIndexOutOfBoundsException {
        throw new InternalError();
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
